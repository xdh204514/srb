package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.*;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.utils.LendNoUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Service
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemReturnMapper lendItemReturnMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String commitReturn(Long lendReturnId, Long userId) {
        // 获取还款记录
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);

        // 判断账号余额是否充足
        BigDecimal amount = userAccountService.getAmount(userId);
        Assert.isTrue(amount.doubleValue() >= lendReturn.getTotal().doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        // 获取借款人code
        String bindCode = userBindService.getBindCodeByUserId(userId);
        // 获取lend
        Long lendId = lendReturn.getLendId();
        Lend lend = lendMapper.selectById(lendId);

        // 组装汇付宝需要的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentGoodsName", lend.getTitle());  // 商户商品名称
        paramMap.put("agentBatchNo",lendReturn.getReturnNo());  // 批次号
        paramMap.put("fromBindCode", bindCode);  // 还款人绑定协议号
        paramMap.put("totalAmt", lendReturn.getTotal());  // 还款总额
        paramMap.put("note", "");  // 说明
        List<Map<String, Object>> lendItemReturnDetailList = lendItemReturnService.addReturnDetail(lendReturnId);
        paramMap.put("data", JSONObject.toJSONString(lendItemReturnDetailList));  // 还款明细
        paramMap.put("voteFeeAmt", new BigDecimal(0));  // 手续费为 0
        paramMap.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        // 构建自动提交表单
        String s = FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);
        return s;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {
        // 1. 获取还款编号
        String agentBatchNo = (String)paramMap.get("agentBatchNo");

        // 2. 幂等性判断
        boolean result = transFlowService.isSaveTransFlow(agentBatchNo);
        if(result){
            log.warn("幂等性返回");
            return "success";
        }

        // 3. 获取还款数据
        QueryWrapper<LendReturn> lendReturnQueryWrapper = new QueryWrapper<>();
        lendReturnQueryWrapper.eq("return_no", agentBatchNo);
        LendReturn lendReturn = baseMapper.selectOne(lendReturnQueryWrapper);

        // 4. 更新还款对象
        String voteFeeAmt = (String)paramMap.get("voteFeeAmt");
        lendReturn.setStatus(1);
        lendReturn.setFee(new BigDecimal(voteFeeAmt));
        lendReturn.setRealReturnTime(LocalDateTime.now());
        baseMapper.updateById(lendReturn);

        // 5. 更新标的信息，如果是最后一次还款，那么就更新标的状态为已结清
        Lend lend = lendMapper.selectById(lendReturn.getLendId());
        if(lendReturn.getLast()){
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());
            lendMapper.updateById(lend);
        }

        // 6. 更新还款账号金额
        BigDecimal totalAmt = new BigDecimal((String)paramMap.get("totalAmt"));
        String bindCode = userBindService.getBindCodeByUserId(lendReturn.getUserId());
        userAccountMapper.updateAccount(bindCode, totalAmt.negate(), new BigDecimal(0));

        // 7. 保存还款流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBatchNo,
                bindCode,
                totalAmt,
                TransTypeEnum.RETURN_DOWN,
                "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);

        // 8. 获取回款明细并进行相应操作
        List<LendItemReturn> lendItemReturnList = lendItemReturnService.selectLendItemReturnList(lendReturn.getId());
        lendItemReturnList.forEach(lendItemReturn -> {
            // 更新回款状态
            lendItemReturn.setStatus(1);
            lendItemReturn.setRealReturnTime(LocalDateTime.now());
            lendItemReturnMapper.updateById(lendItemReturn);

            // 更新出借信息
            LendItem lendItem = lendItemMapper.selectById(lendItemReturn.getLendItemId());
            lendItem.setRealAmount(lendItem.getRealAmount().add(lendItemReturn.getInterest())); //动态的实际收益
            lendItemMapper.updateById(lendItem);

            // 投资账号转入金额
            String investBindCode = userBindService.getBindCodeByUserId(lendItemReturn.getInvestUserId());
            userAccountMapper.updateAccount(investBindCode, lendItemReturn.getTotal(), new BigDecimal(0));

            // 回款流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtil.getReturnItemNo(),
                    investBindCode,
                    lendItemReturn.getTotal(),
                    TransTypeEnum.INVEST_BACK,
                    "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
            transFlowService.saveTransFlow(investTransFlowBO);
        });

        return "success";
    }
}

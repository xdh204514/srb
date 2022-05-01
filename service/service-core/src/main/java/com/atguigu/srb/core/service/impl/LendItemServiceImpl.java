package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendItemStatusEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.LendItemMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.vo.InvestVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.utils.LendNoUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Slf4j
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendService lendService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String commitInvest(InvestVO investVO) {

        // 1. 通过 lendId 获取 lend
        Long lendId = investVO.getLendId();
        Lend lend = lendMapper.selectById(lendId);

        // 2. 获取投资人Id 和 借款人Id
        Long investUserId = investVO.getInvestUserId();
        Long benefitUserId = lend.getUserId();

        // 3. 校验：标的状态，校验标准：募资中
        Assert.isTrue(
                lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus().intValue(),
                ResponseEnum.LEND_INVEST_ERROR);

        // 4. 校验：标的不能超卖，校验标准：已投金额 + 本次投资金额  >= 标的金额
        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(),
                ResponseEnum.LEND_FULL_SCALE_ERROR);

        // 5. 校验：账户可用余额充足，校验标准：当前用户的余额 >= 当前用户此次的投资金额
        BigDecimal amount = userAccountService.getAmount(investUserId); //获取当前用户的账户余额
        Assert.isTrue(amount.doubleValue() >= Double.parseDouble(investVO.getInvestAmount()),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        // 6. 获取投资人的 bindCode 和 借款人的 bindCode
        String investBindCode = userBindService.getBindCodeByUserId(investUserId);
        String benefitBindCode = userBindService.getBindCodeByUserId(benefitUserId);

        // 7. 生成 LendItem 对象
        LendItem lendItem = new LendItem();
        lendItem.setInvestUserId(investUserId);  // 投资人Id
        lendItem.setInvestName(investVO.getInvestName());  // 投资人名字
        String lendItemNo = LendNoUtil.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo);   // 投资条目编号(一个 Lend 对应一个或多个LendItem)
        lendItem.setLendId(investVO.getLendId());  // 对应的标的Id
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount()));  // 此次投资金额
        lendItem.setLendYearRate(lend.getLendYearRate());  // 年化
        lendItem.setInvestTime(LocalDateTime.now());  // 投资时间
        lendItem.setLendStartDate(lend.getLendStartDate());  // 开始时间
        lendItem.setLendEndDate(lend.getLendEndDate());  // 结束时间
        BigDecimal expectAmount = lendService.getInterestCount(
                lendItem.getInvestAmount(),
                lendItem.getLendYearRate(),
                lend.getPeriod(),
                lend.getReturnMethod());
        lendItem.setExpectAmount(expectAmount);  //预期收益
        lendItem.setRealAmount(new BigDecimal(0));  // 实际收益(还款后才有实际收益)  //何时预期收益和实际收益不一致？当该投资项目为满标的时候(即未筹借满)，那么当开始还款的时候肯定就会变得更少
        lendItem.setStatus(LendItemStatusEnum.DEFAULT.getStatus());  // 刚刚创建是为默认状态，回调完成后变为已支付状态，还款结束变成已还款状态
        baseMapper.insert(lendItem);

        // 8. 封装提交至汇付宝的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", investBindCode);
        paramMap.put("benefitBindCode", benefitBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());  // 项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        // 9. 在资金托管平台上的投资订单的唯一编号，要和 lendItemNo 保持一致。
        paramMap.put("agentBillNo", lendItemNo);  // 订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount());  // 标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        // 10. 构建充值自动提交表单
        return FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {

        // 1. 获取投资编号(也即 TransFlow 中的 trans_no)
        String agentBillNo = (String) paramMap.get("agentBillNo");

        // 2. 幂等性判断(通过流水号)
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if (result) {
            log.info("幂等性判断：已经保存过了");
            return "success";
        }

        // 3. 获取投资用户的 bindCode 和此次投资金额
        String bindCode = (String) paramMap.get("voteBindCode");
        String voteAmt = (String) paramMap.get("voteAmt");

        // 4. 根据 bindCode 更新投资人的账号金额等信息：账户可用余额 - 此次投资金额，冻结金额 + 此次投资金额
        userAccountMapper.updateAccount(bindCode, new BigDecimal("-" + voteAmt), new BigDecimal(voteAmt));

        // 5. 将投资记录中的状态字段由默认更新为已支付
        LendItem lendItem = this.getLendItemByNo(agentBillNo);
        lendItem.setStatus(LendItemStatusEnum.PAID.getStatus());
        baseMapper.updateById(lendItem);

        // 6. 更新标的的投资人和已获取到的投资金额
        Long lendId = lendItem.getLendId();
        Lend lend = lendMapper.selectById(lendId);
        lend.setInvestNum(lend.getInvestNum() + 1);
        lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));
        lendMapper.updateById(lend);

        // 7. 新增交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
            agentBillNo,
            bindCode,
            new BigDecimal(voteAmt),
            TransTypeEnum.INVEST_LOCK,
            "投资项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);

        return "success";
    }

    @Override
    public List<LendItem> selectByLendId(Long lendId, Integer status) {
        QueryWrapper<LendItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_id", lendId).eq("status", status);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<LendItem> selectByLendId(Long lendId) {
        QueryWrapper<LendItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 通过 lend_item_no 获取到 LendItem 对象
     *
     * @param lendItemNo lendItemNo
     * @return LendItem 对象
     */
    private LendItem getLendItemByNo(String lendItemNo) {
        QueryWrapper<LendItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_item_no", lendItemNo);
        return baseMapper.selectOne(queryWrapper);
    }
}

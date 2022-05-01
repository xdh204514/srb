package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.FormHelper;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.service.TransFlowService;
import com.atguigu.srb.core.service.UserAccountService;
import com.atguigu.srb.core.service.UserBindService;
import com.atguigu.srb.core.utils.LendNoUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Override
    public String commitCharge(String chargeAmt, Long userId) {

        // 1. 获取 UserInfo
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();

        // 2. 判断账户绑定状态
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);

        // 3. 根据汇付宝提交接口，组装参数
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtil.getChargeNo());  // 充值编号(在后面幂等性判断的时候用的就是这个编号)
        paramMap.put("bindCode", bindCode);
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigDecimal("0"));  // 不做冻结功能，因为硬编码为 0
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);   // 回调通知地址
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);  // 正常返回地址
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        // 4. 构建充值自动提交表单
        return FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {

        // 1. 获取商户充值订单号
        String agentBillNo = (String) paramMap.get("agentBillNo");

        // 2. 判断流水是否存在
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if (isSave) {
            log.warn("幂等性返回");
            return "success";
        }

        // 3. 获取充值人绑定协议号
        String bindCode = (String) paramMap.get("bindCode");

        // 4. 获取充值金额
        String chargeAmt = (String) paramMap.get("chargeAmt");

        // 5. 根据 bindCode 去更新账户的金额和冻结金额(实际上冻结金额一直不变)
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));

        // 6. 增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值");
        transFlowService.saveTransFlow(transFlowBO);

        // 在实际充值成功(汇付宝和尚融宝的 account 数据已经同步成功)的情况下，为了验证幂等性的成功与否，在这里返回 fail
        // 这样汇付宝会因为返回的结果不是 success，而重新发起异步通知
        // 但是因为存在幂等性判断(即存在一样的交易流水号)，所以在第二次异步通知中会返回 success
        // 那么汇付宝就不会发起第三次异步通知
        // return "fail";
        return "success";
    }

    @Override
    public BigDecimal getAmount(Long userId) {
        QueryWrapper<UserAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserAccount userAccount = baseMapper.selectOne(queryWrapper);
        return userAccount.getAmount();
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        // 1. 判断账户余额是否超过提现金额
        BigDecimal amount = userAccountService.getAmount(userId);
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        // 2. 组装汇付宝需要的参数
        Map<String, Object> paramMap = new HashMap<>();
        String bindCode = userBindService.getBindCodeByUserId(userId);
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtil.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        // 3. 构建自动提交表单
        return FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notifyWithdraw(Map<String, Object> paramMap) {

        // 1. 获取相应参数
        String agentBillNo = (String) paramMap.get("agentBillNo");
        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");

        // 2. 接口幂等性判断
        if (transFlowService.isSaveTransFlow(agentBillNo)) {
            log.warn("幂等性返回");
            return "success";
        }

        // 3. 更新账户金额
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));

        // 4. 增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW,
                "用户提现：" + fetchAmt
        );
        transFlowService.saveTransFlow(transFlowBO);
        return "success";
    }
}

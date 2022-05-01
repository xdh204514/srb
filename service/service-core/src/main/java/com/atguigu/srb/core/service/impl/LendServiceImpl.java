package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendItemStatusEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.ReturnMethodEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.hfb.HfbConst;
import com.atguigu.srb.core.hfb.RequestHelper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.*;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Slf4j
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    // TODO：优化同一个 BorrowInfo 只能创建一个标的，那么需要增加判断
    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(LendNoUtil.getLendNo());
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setLowestAmount(new BigDecimal(100));
        lend.setInvestAmount(new BigDecimal(0));
        lend.setInvestNum(0);
        lend.setPublishDate(LocalDateTime.now());
        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());

        // 因为管理员可以修改年化利率，所以从审批对象中获取
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));
        // 因为管理员可以修改平台服务费，所以从审批对象中获取
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));
        // 起息日期
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dtf);
        lend.setLendStartDate(lendStartDate);
        // 结束日期
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);
        // 月年化 = 年化 / 12
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        // 平台预期收益 = 标的金额 * 月年化 * 期数
        BigDecimal expectAmount = lend.getAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        lend.setExpectAmount(expectAmount);
        // 标的结束才能计算实际收益，目前设置为 0
        lend.setRealAmount(new BigDecimal(0));
        // 状态
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        // 审核时间
        lend.setCheckTime(LocalDateTime.now());
        // 审核人
        lend.setCheckAdminId(1L);

        baseMapper.insert(lend);
    }

    // TODO：关键词查询中需要增加“还款方式”查询，那么就需要自定义 mapper 查询
    @Override
    public IPage<Lend> getLendList(Page<Lend> pageParam, String keyword) {
        Page<Lend> lendPage = null;
        if (keyword.equals("")) {
            lendPage = baseMapper.selectPage(pageParam, null);
        } else {
            QueryWrapper<Lend> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("title", keyword).or()
                    .like("lend_info", keyword).or()
                    .orderByDesc("update_time");
            lendPage = baseMapper.selectPage(pageParam, queryWrapper);
        }
        return lendPage.convert(lend -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
            return lend;
        });
    }

    @Override
    public List<Lend> getLendList() {
        List<Lend> lends = baseMapper.selectList(null);
        lends.forEach(lend -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
        });
        return lends;
    }

    @Override
    public Map<String, Object> getLendDetail(Long id) {
        // 查询标的对象
        Lend lend = baseMapper.selectById(id);
        Assert.notNull(lend, ResponseEnum.LEND_IS_NOT_EXIST);

        // 获取额外参数
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);

        // 根据user_id获取借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        // 组装借款人对象
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        // 组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);

        // 返回数据
        return result;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod) {

        BigDecimal interestCount;
        // 计算总利息
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalMonth);
        } else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalMonth);
        }
        return interestCount;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void makeLoan(Long id) {
        // 1. 放款
        // 1.1 获取标的信息
        Lend lend = baseMapper.selectById(id);

        // 1.2 组装汇付宝放款接口需要的参数
        Map<String, Object> paramMap = new HashMap<>();
        String loanNo = LendNoUtil.getLoanNo();
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        BigDecimal mchFee = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));  // 平台实际收益 = 已投金额 * 月年化 * 标的期数
        paramMap.put("agentId", HfbConst.AGENT_ID);                // agent_id：给商户分配的唯一标识
        paramMap.put("agentProjectCode", lend.getLendNo());        // agent_project_code：放款项目编号
        paramMap.put("agentBillNo", loanNo);                       // agent_bill_no：放款编号
        paramMap.put("mchFee", mchFee);                            // mch_fee：商户手续费，即平台实际收益，与平台预期收益可能会因为没有满标而存在差别
        paramMap.put("timestamp", RequestHelper.getTimestamp());   // timestamp：时间戳
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);                                // sign：验签参数
        log.info("放款参数：" + JSONObject.toJSONString(paramMap));

        // 1.3 向汇付宝发送请求进行放款
        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("汇付宝放款回调结果：" + result.toJSONString());

        // 1.4 对回调结果进行判断决定是否进行业务处理
        if (!"0000".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }

        // 2. 放款成功则处理业务如下
        // 2.1. 更新标的状态、标的平台收益、标的放款时间
        lend.setRealAmount(mchFee);
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(lend);

        // 2.2. 更新借款人账号金额(即将投资人的投款金额转入借款人账号中)
        BigDecimal realInvestAmount = new BigDecimal(result.getString("voteAmt"));  // 真正拿到的投资金额
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String borrowerBindCode = userInfo.getBindCode();  // 借款人的 bind_code
        userAccountMapper.updateAccount(borrowerBindCode, realInvestAmount, new BigDecimal(0));  // 根据 bind_code 更新账户信息

        // 2.3. 增加借款人接到放款金额的交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                loanNo,
                borrowerBindCode,
                realInvestAmount,
                TransTypeEnum.BORROW_BACK,
                "借款人借款到账交易：放款编号：" + loanNo);
        transFlowService.saveTransFlow(transFlowBO);

        // 2.4. 解冻并扣除该标的所有投资人的投资资金
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), LendItemStatusEnum.PAID.getStatus());
        lendItemList.stream().forEach(lendItem -> {
            Long investUserId = lendItem.getInvestUserId();  // 投资人Id
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);  // 投资人信息
            String investBindCode = investUserInfo.getBindCode();  // 投资人BindCode
            BigDecimal investAmount = lendItem.getInvestAmount();  // 投资人本次投资金额
            userAccountMapper.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());

            // 2.5. 增加投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtil.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "投资人的冻结资金转出：投资时的编号：" + lend.getLendNo());
            transFlowService.saveTransFlow(investTransFlowBO);
        });

        // 2.6. 生成借款人还款计划和出借人回款计划
        this.repaymentPlan(lend);
    }

    /**
     * 借款人还款计划
     *
     * @param lend 标的
     */
    private void repaymentPlan(Lend lend) {
        // 1. 创建还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        // 2. 根据还款期数，生成每一期的还款对象
        int periodNum = lend.getPeriod();
        for (int i = 1; i <= periodNum; i++) {
            LendReturn lendReturn = new LendReturn();
            String lendReturnNo = LendNoUtil.getReturnNo();
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setReturnNo(lendReturnNo);
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setCurrentPeriod(i);
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setReturnMethod(lend.getReturnMethod());
            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i));
            lendReturn.setOverdue(false);
            lendReturn.setLast(i == periodNum);
            lendReturn.setStatus(0);
            // principal、interest 和 total 这三个字段的值需要先通过计算回款计划中相对应的数据后，才能得到
            // 比如：这一期需要还款的本金 = SUM(这一期每一个投资人能够得到的回款本金)
            // 比如：这一期需要还款的利息 = SUM(这一期每一个投资人能够得到的回款利息)
            // 比如：这一期需要还款的总和 = SUM(这一期需要回款本金, 这一期需要回款利息)
            lendReturnList.add(lendReturn);
        }
        // 3. 批量保存还款计划
        lendReturnService.saveBatch(lendReturnList);

        // 4. 获取 lendReturnList 中还款期数与还款计划id对应集合
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );
        log.info("lendReturnMap：" + JSONObject.toJSONString(lendReturnMap));

        // 5. 创建所有回款计划列表(每一个投资人都有 periodNum 个回款计划)
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();

        // 6. 获取该标的所有投资成功的记录，并遍历
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), LendItemStatusEnum.PAID.getStatus());
        lendItemList.forEach(lendItem -> {
            // 7. 为每一个投资人生成回款
            // TODO：这里不能直接获取方法返回的 lendItemReturnList 进行计算，需要从数据库中查询得到
            // 因为在 returnInvest 方法中保存 lendItemReturnList 的时候会因为 BigDecimal 类型的数据保存的时候只保留两位小数而导致
            // 返回的 lendItemReturnList 和从数据库中获取的 lendItemReturnList 在某些字段上的数值存在不一致
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            // 8. 将每一个投资人的回款计划加入总回款计划中
            lendItemReturnAllList.addAll(lendItemReturnList);
        });

        // 9. 更新还款计划中的 principal、interest、total 字段
        lendReturnList.forEach(lendReturn -> {
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    // 过滤条件：当回款计划中的还款计划id == 当前还款计划id的时候
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    // 将所有回款计划中计算的每月应收本金相加
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lendReturn.setPrincipal(sumPrincipal);  // 每期还款本金
            lendReturn.setInterest(sumInterest);  // 每期还款利息
            lendReturn.setTotal(sumTotal);  // 每期还款本息
        });

        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 投资人回款计划
     *
     * @param lendItemId    LendItem的Id
     * @param lendReturnMap 集合
     * @param lend          标的
     * @return 集合
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {

        // 1. 根据还款方式计算利息和本金
        LendItem lendItem = lendItemService.getById(lendItemId);  // 投标信息
        BigDecimal amount = lendItem.getInvestAmount();  // 投资金额
        BigDecimal yearRate = lendItem.getLendYearRate();  // 年化利率
        int totalMonth = lend.getPeriod();  // 投资期数
        Map<Integer, BigDecimal> mapInterest = null;  // <key, value> = <还款期数, 利息>
        Map<Integer, BigDecimal> mapPrincipal = null; // <key, value> = <还款期数, 本金>
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        // 2. 生成回款列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            LendItemReturn lendItemReturn = new LendItemReturn();
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            // 最后一次本金、利息计算
            if (lendItemReturnList.size() > 0 && currentPeriod.intValue() == lend.getPeriod().intValue()) {
                // 除了最后一期前面期数计算出来的所有的应还本金
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                // 最后一期应还本金 = 用当前投资人的总投资金额 - 除了最后一期前面期数计算出来的所有的应还本金
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);
                // 最后一期利息 = 总利息 - 除了最后一期前面期数计算出来的所有的利息
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);  // 确定是预期收益吗？
                lendItemReturn.setInterest(lastInterest);
            } else {
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            lendItemReturnList.add(lendItemReturn);
        }
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }
}

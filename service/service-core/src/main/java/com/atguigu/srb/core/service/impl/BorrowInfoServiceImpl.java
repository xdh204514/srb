package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.BorrowInfoStatusEnum;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.BorrowInfoMapper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.IntegralGradeMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.atguigu.srb.core.service.LendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        // 根据 userId 获取用户积分
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        Integer integral = userInfo.getIntegral();

        // 根据用户积分查询额度
        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper.le("integral_start", integral)
                .ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeMapper.selectOne(integralGradeQueryWrapper);
        if (integralGrade == null) {
            return new BigDecimal(0);
        }

        return integralGrade.getBorrowAmount();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {

        // 获取用户 UserInfo
        UserInfo userInfo = userInfoMapper.selectById(userId);

        // 1. 判断用户的账户绑定状态
        Assert.isTrue(
                userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);

        // 2. 判断用户的借款额度认证状态
        // 因为在 BorrowerServiceImpl 中更新 UserInfo.status 用的就是 BorrowerStatusEnum，所以判断的时候也需要用这个
        // userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        Assert.isTrue(
                userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        // 3. 判断用户申请的借款额度是否超过实际的借款额度
        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        // 4. 保存数据
        borrowInfo.setUserId(userId);  // 设置 user_id
        borrowInfo.setBorrowYearRate( borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));  // 将百分比转换为小数
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());  // 设置借款申请状态为审核中
        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getBorrowInfoStatus(Long userId) {
        QueryWrapper<BorrowInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(queryWrapper);
        if (objects.size() == 0) {
            // 借款人尚未提交借款信息
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }
        return (Integer)objects.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public IPage<BorrowInfoVO> getBorrowInfoVOList(Page<BorrowInfoVO> pageParam, String keyword) {
        IPage<BorrowInfoVO>  borrowInfoVOPage = null;
        QueryWrapper<BorrowInfoVO> queryWrapper = new QueryWrapper<>();
        if (keyword.equals("")) {
            queryWrapper.eq("bi.is_deleted", 0)
                    .orderByDesc("update_time");;
        } else {
            queryWrapper.eq("bi.is_deleted", 0)
                    .like("b.name", keyword).or()
                    .like("b.mobile", keyword)
                    .orderByDesc("update_time");
        }
        borrowInfoVOPage =baseMapper.selectPageByBorrowInfoVO(pageParam, queryWrapper);
        return borrowInfoVOPage.convert(borrowInfoVO -> {
            // 将 borrowInfoVO 对象中的 returnMethod、moneyUse、status 字段中的数字通过查询字典变为名字，即 1 >>> 等额本息
            BorrowInfoVO temp = new BorrowInfoVO();
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", Integer.parseInt(borrowInfoVO.getReturnMethod()));
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", Integer.parseInt(borrowInfoVO.getMoneyUse()));
            String status = BorrowInfoStatusEnum.getMsgByStatus(Integer.parseInt(borrowInfoVO.getStatus()));

            // 复制
            BeanUtils.copyProperties(borrowInfoVO, temp);
            // 重新修改 returnMethod、moneyUse、status 字段值
            temp.setStatus(status);
            temp.setReturnMethod(returnMethod);
            temp.setMoneyUse(moneyUse);
            return temp;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        // 1. 获取 BorrowInfo 对象
        BorrowInfo borrowInfo = baseMapper.selectById(id);

        // 2. 将 BorrowInfo 对象 copy 到 BorrowInfoVO 中
        BorrowInfoVO borrowInfoVO = new BorrowInfoVO();
        BeanUtils.copyProperties(borrowInfo, borrowInfoVO);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfoVO.setStatus(status);
        borrowInfoVO.setReturnMethod(returnMethod);
        borrowInfoVO.setMoneyUse(moneyUse);

        // 3. 根据 user_id 获取借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);

        // 4. 获取借款人详情
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        // 5. 设置 borrowInfoVO 中的 name 和 mobile 字段(可选操作)
        borrowInfoVO.setName(borrowerDetailVO.getName());
        borrowInfoVO.setMobile(borrowerDetailVO.getMobile());

        HashMap<String, Object> result = new HashMap<>();
        result.put("borrower", borrowerDetailVO);
        result.put("borrowInfo", borrowInfoVO);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        // 修改借款信息状态
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);

        // 审核通过则创建标的
        if (borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()) {
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
        }
    }
}

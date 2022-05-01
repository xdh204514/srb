package com.atguigu.srb.core.mapper;

import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {
    IPage<BorrowInfoVO> selectPageByBorrowInfoVO(Page<BorrowInfoVO> pageParam, @Param(Constants.WRAPPER)QueryWrapper<BorrowInfoVO> queryWrapper);
}

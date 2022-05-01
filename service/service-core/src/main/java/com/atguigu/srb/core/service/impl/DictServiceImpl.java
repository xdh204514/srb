package com.atguigu.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.core.listener.ExcelDictDTOListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.pojo.entity.ExcelDictDTO;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

// 如果当前注入的 mapper 就是当前 service 的 mapper 则可以不用显示声明，直接使用 baseMapper 就行
//    @Resource
//    private DictMapper dictMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)  // 出现异常则全部回滚
    @Override
    public void importExcelData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }

    @Override
    public List getDictData() {
        List<Dict> dictList = baseMapper.selectList(null);
        // 创建 ExcelDictDTO 列表，将 Dict 列表转换成 ExcelDictDTO 列表（因为 EasyExcel 需要的是 ExcelDictDTO）
        ArrayList<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    @Override
    public List<Dict> getDictDataByParentId(Long parentId) {

        List<Dict> dictDataList = null;

        // 1. 首先从判断 redis 中是否有数据，有则从 redis 中取数据
        try {
            dictDataList = (List<Dict>) redisTemplate.opsForValue().get("srb:core:dictDataList:" + parentId);
            if (dictDataList != null) {
                log.info("从 Redis 中获取到数据字典");
                return dictDataList;
            }
        } catch (Exception e) {  // 使用 try/catch 是为了保证出异常的时候能够捕获到，然后处理完继续执行后续代码，否则抛出异常不会执行后续代码
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));  // 此处不抛出异常，继续执行后面的代码
        }

        // 2. 没有或者是redis宕机则从数据库中取数据
        log.info("从数据库中获取数据字典");
        dictDataList = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));
        // 判断父节点中是否真的含有子节点
        dictDataList.forEach(dict -> {
            boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });

        // 3. 将从数据库中取出的数据存入 redis 中
        try {
            redisTemplate.opsForValue().set("srb:core:dictDataList:" + parentId, dictDataList, 5, TimeUnit.MINUTES);
            log.info("数据字典存入Redis");
        } catch (Exception e) {
            log.error("Redis服务器异常：" + ExceptionUtils.getStackTrace(e)); // 此处不抛出异常，继续执行后面的代码
        }

        // 4. 返回数据到 controller 层
        return dictDataList;
    }

    @Override
    public List<Dict> getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        final Dict dict = baseMapper.selectOne(queryWrapper);
        return this.getDictDataByParentId(dict.getId());
    }

    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, Integer value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        Dict parentDict = baseMapper.selectOne(queryWrapper);
        if (parentDict == null) {
            return "";
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentDict.getId())
                .eq("value", value);
        Dict dict = baseMapper.selectOne(queryWrapper);

        return dict == null ? "" : dict.getName();
    }

    /**
     * 判断是否有子节点
     *
     * @param id 父节点id
     * @return 是否存在子节点
     */
    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<Dict>().eq("parent_id", id);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }
}

package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
public interface DictService extends IService<Dict> {
    void importExcelData(InputStream inputStream);

    List getDictData();

    List<Dict> getDictDataByParentId(Long parentId);

    List<Dict> getDictByDictCode(String dictCode);

    String getNameByParentDictCodeAndValue(String dictCode, Integer value);
}

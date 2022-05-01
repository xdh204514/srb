package com.atguigu.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.entity.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author coderxdh
 * @create 2022-04-18 21:00
 */
@Slf4j
@NoArgsConstructor //无参
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    // 每隔 BATCH_COUNT 进行数据存储
    private static final int BATCH_COUNT = 5;

    // 将 BATCH_COUNT 条数据记录存储到 list 中，然后统一存储到数据库中
    private List<ExcelDictDTO> list = new ArrayList<>();

// 这种方式是错的，因为能进行注入的前提是：该类也被 Spring 管理了，但是该类不能被 Spring 管理，因为 AnalysisEventListener 都没有被管理
//    @Resource
//    private DictMapper dictMapper;

    // 通过构造函数传入 dictMapper 对象
    private DictMapper dictMapper;

    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }


    @Override
    public void invoke(ExcelDictDTO excelDictDTO, AnalysisContext analysisContext) {

        list.add(excelDictDTO);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 为了保证不满 BATCH_COUNT 条记录也能存储到数据库，需要在完成解析的时候再进行一次数据库存储
        saveData();
    }


    private void saveData() {
        log.info("开始存储{}条数据！", list.size());
        dictMapper.insertBatch(list);  // 批量插入
        log.info("完成存储{}条数据！", list.size());
    }
}

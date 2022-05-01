package com.heepay.config;

import com.alibaba.fastjson.JSON;
import com.heepay.util.SignUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class NotifyThread implements Runnable {

    private int count = 1;
    private String notifyUrl;
    private Map<String, Object> paramMap;

    public NotifyThread(){}

    public NotifyThread(String notifyUrl, Map<String, Object> paramMap) {
        this.notifyUrl = notifyUrl;
        this.paramMap = paramMap;
    }

    @Override
    public void run() {
        task();
    }

    private void task() {
        // 根据 notifyUrl 发送 POST 请求到商户平台(尚融宝)
        String result = SignUtil.sendRequest(paramMap,notifyUrl);
        log.info(notifyUrl + "：" + result + " count：" + count);
        // 可以看出商户平台(尚融宝)必须响应 success 才算成功，其他返回结果都算是失败，会进行失败重试
        if(!"success".equals(result)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //ScheduledTask.queue.offer(new NotifyVo(notifyUrl, paramMap));
            count++;
            if(count <= 5) {
                task();
                log.info("失败重试：" + JSON.toJSONString(this));
            }
        }
    }
}

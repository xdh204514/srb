package com.atguigu.srb.sms.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author coderxdh
 * @create 2022-04-19 21:51
 */
@Service
public interface SmsService {
    void send(String mobile, String templateCode, Map<String,Object> param);
}

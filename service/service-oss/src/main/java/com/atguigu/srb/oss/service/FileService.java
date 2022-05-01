package com.atguigu.srb.oss.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * @author coderxdh
 * @create 2022-04-20 13:12
 */
@Service
public interface FileService {

    String upload(InputStream inputStream, String module, String fileName);

    void removeFile(String url);
}

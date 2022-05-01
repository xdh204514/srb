package com.atguigu.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.oss.service.FileService;
import com.atguigu.srb.oss.utils.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author coderxdh
 * @create 2022-04-20 13:13
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Override
    public String upload(InputStream inputStream, String module, String fileName) {

        // 1. 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(
                OssProperties.ENDPOINT,
                OssProperties.KEY_ID,
                OssProperties.KEY_SECRET);
        String fileRootPath = "";
        try {
            // 2. 判断存储空间是否存在，不存在则创建
            if (!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)) {
                // 创建 CreateBucketRequest 对象
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(OssProperties.BUCKET_NAME);
                // 设置存储空间的权限为公共读，默认为私有
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
            }

            // 3. 构建文件全路径
            // 以日期作为存储目录结构
            String folder = new DateTime().toString("yyyy/MM/dd");
            // 以uuid.扩展名作为文件名(因为用户的文件名不可控，所以需要自定义文件名)
            fileName = UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf("."));
            // 文件全路径
            fileRootPath = module + "/" + folder + "/" + fileName;

            // 4. 上传文件到阿里云
            ossClient.putObject(OssProperties.BUCKET_NAME, fileRootPath, inputStream);
        } catch (OSSException e) {
            log.error("阿里云OSS服务调用失败：");
            log.error("ErrorCode=" + e.getErrorCode());
            log.error("ErrorMessage=" + e.getErrorMessage());
            throw new BusinessException(ResponseEnum.ALIYUN_OSS_ERROR , e);
        } finally {
            if (ossClient != null) {
                // 5. 关闭 OSSClient
                ossClient.shutdown();
            }
        }

        // 6. 返回阿里云文件绝对路径
        return "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/" + fileRootPath;
    }

    @Override
    public void removeFile(String url) {
        // 1. 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(
                OssProperties.ENDPOINT,
                OssProperties.KEY_ID,
                OssProperties.KEY_SECRET);
        String fileRootPath = "";

        // 2. 文件名（服务器上的文件路径）
        // 即将上传过来的 URL：https://srb-xdh.oss-cn-beijing.aliyuncs.com/wallpaper/2022/04/20/a1f3f352-0b4f-4a2a-8a42-1bae160c00e6.png
        // 截取为 objectName：wallpaper/2022/04/20/a1f3f352-0b4f-4a2a-8a42-1bae160c00e6.png
        String host = "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/";
        String objectName = url.substring(host.length());

        // 3. 删除文件
        ossClient.deleteObject(OssProperties.BUCKET_NAME, objectName);

        // 4. 关闭OSSClient
        ossClient.shutdown();
    }
}

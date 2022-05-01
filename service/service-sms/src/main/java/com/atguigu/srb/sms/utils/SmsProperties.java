package com.atguigu.srb.sms.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author coderxdh
 * @create 2022-04-19 21:38
 */

@Component
@ConfigurationProperties(prefix = "aliyun.sms")
@Setter
@Getter //idea2020.2.3版配置文件自动提示需要这个
public class SmsProperties implements InitializingBean {
    // 整个过程就是，程序一启动，通过 Component 注解创建出该对象
    // 再通过 ConfigurationProperties 注解以及 prefix 去读取配置文件中的配置好的值
    // 接着通过 Setter 进行赋值
    // 然后通过重写 afterPropertiesSet() 方法将其赋给静态变量
    // 最后通过 SmsProperties.静态变量的方式去使用
    private String regionId;
    private String keyId;
    private String keySecret;
    private String loginTemplateCode;
    private String registerTemplateCode;
    private String resetTemplateCode;
    private String signName;

    public static String REGION_Id;
    public static String KEY_ID;
    public static String KEY_SECRET;
    public static String LOGIN_TEMPLATE_CODE;
    public static String REGISTER_TEMPLATE_CODE;
    public static String RESET_TEMPLATE_CODE;
    public static String SIGN_NAME;

    // 当私有成员被赋值后，此方法自动被调用，从而初始化常量
    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_Id = regionId;
        KEY_ID = keyId;
        KEY_SECRET = keySecret;
        LOGIN_TEMPLATE_CODE = loginTemplateCode;
        REGISTER_TEMPLATE_CODE = resetTemplateCode;
        RESET_TEMPLATE_CODE = resetTemplateCode;
        SIGN_NAME = signName;
    }
}

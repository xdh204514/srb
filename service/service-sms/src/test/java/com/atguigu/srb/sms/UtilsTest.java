package com.atguigu.srb.sms;

import com.atguigu.srb.sms.utils.SmsProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author coderxdh
 * @create 2022-04-19 21:46
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UtilsTest {

    @Test
    public void testProperties(){
        System.out.println(SmsProperties.KEY_ID);
        System.out.println(SmsProperties.KEY_SECRET);
        System.out.println(SmsProperties.REGION_Id);
        System.out.println(SmsProperties.REGISTER_TEMPLATE_CODE);
        System.out.println(SmsProperties.LOGIN_TEMPLATE_CODE);
        System.out.println(SmsProperties.RESET_TEMPLATE_CODE);
    }
}

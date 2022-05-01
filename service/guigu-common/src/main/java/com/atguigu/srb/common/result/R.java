package com.atguigu.srb.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author coderxdh
 * @create 2022-04-17 22:31
 */
@Data
public class R {
    private Integer code;

    private String message;

    private Map<String, Object> data = new HashMap();

    /**
     * 构造器私有，保证其他类无法通过构造器创建返回对象
     */
    private R(){}

    /**
     * 返回成功
     * @return  返回R对象，为了在请求的返回值中能够使用串联语法(return R.ok().data("所有等级列表", list);)，那么就需要返回R对象，这样才能够保证串联使用
     */
    public static R ok(){
        R r = new R();
        r.setCode(ResponseEnum.SUCCESS.getCode());
        r.setMessage(ResponseEnum.SUCCESS.getMessage());
        return r;
    }

    /**
     * 返回失败
     */
    public static R error(){
        R r = new R();
        r.setCode(ResponseEnum.ERROR.getCode());
        r.setMessage(ResponseEnum.ERROR.getMessage());
        return r;
    }

    /**
     * 设置特定结果
     * @param responseEnum  枚举对象
     * @return  返回结果
     */
    public static R setResult(ResponseEnum responseEnum){
        R r = new R();
        r.setCode(responseEnum.getCode());
        r.setMessage(responseEnum.getMessage());
        return r;
    }

    /**
     * 设置特定的响应消息
     * @param message  响应消息
     * @return 返回 R 对象
     */
    public R message(String message){
        this.setMessage(message);
        return this;
    }

    /**
     * 设置特定的响应码
     * @param code 响应码
     * @return 返回 R 对象
     */
    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    /**
     * 以 <key, value> 的方式设置 data 对象
     * @param key  key
     * @param value  value
     * @return  返回 R 对象，保证串联使用
     */
    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    /**
     * 如果返回的数据是集合，那么直接以 map 的方式设置 data 对象
     * @param map  map 对象
     * @return 返回 R 对象
     */
    public R data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}

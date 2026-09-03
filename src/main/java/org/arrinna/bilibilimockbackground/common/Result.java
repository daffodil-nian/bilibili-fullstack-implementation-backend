package org.arrinna.bilibilimockbackground.common;

import lombok.Data;

@Data
public class Result <T>{
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> Success(T data){
        Result<T> result=new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMsg("设置成功");
        return result;
    }
    public static <T> Result<T> Success(T data,String msg){
        Result<T> result=new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }
    public static <T> Result<T> Success(){
        Result<T> result=new Result<>();
        result.setData(null);
        result.setMsg("成功");
        result.setCode(200);
        return result;
    }
    public static <T> Result<T> Success(String msg){
        Result<T> result=new Result<>();
        result.setData(null);
        result.setMsg(msg);
        result.setCode(200);
        return result;
    }
    public static <T> Result<T> fail(String msg){
        Result<T> result=new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
    public static <T> Result<T> fail(Integer code,String msg){
        Result<T> result=new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}

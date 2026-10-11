package cn.edu.hebau.aitravel.util;

import lombok.Data;

/** 接口统一返回格式，T 表示 data 中的数据类型。 */
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    /** 包装带数据的成功响应。 */
    public static <T> Result<T> success(T data) {
        return success("success", data);
    }

    /** 成功响应，并自定义提示，例如“注册成功”。 */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /** 失败响应，data 固定为 null。 */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}

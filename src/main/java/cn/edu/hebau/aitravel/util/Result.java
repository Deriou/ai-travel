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
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }
}

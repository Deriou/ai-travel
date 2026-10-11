package cn.edu.hebau.aitravel.util;

/** 参数或业务校验不通过时抛出，由全局异常处理转换为 HTTP 400 和 code 400。 */
public class BusinessException extends RuntimeException {
    public BusinessException(String msg) {
        super(msg);
    }
}

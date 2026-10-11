package cn.edu.hebau.aitravel.util;

/** 调用大模型失败（超时、网络异常、返回内容为空）时抛出，由全局异常处理转换为 HTTP 500 和 code 500。 */
public class AiException extends RuntimeException {
    public AiException(String msg) {
        super(msg);
    }
}

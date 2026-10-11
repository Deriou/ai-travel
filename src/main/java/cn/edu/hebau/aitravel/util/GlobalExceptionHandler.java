package cn.edu.hebau.aitravel.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 把各种异常统一转换为 Result 格式，页面只看到简明提示，详细信息留在日志。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务校验失败，例如用户名重复、天数非法。 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, e.getMessage()));
    }

    /** 参数缺失、类型错误、JSON 格式错误或请求方式不对。 */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<Result<Void>> handleBadRequest(Exception e) {
        log.warn("请求参数错误：{}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, "请求参数格式不正确"));
    }

    /** 访问了不存在的路径。 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "接口不存在"));
    }

    /** 其他未预料的异常：记录完整日志，页面只提示服务异常。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("服务异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "服务异常，请稍后重试"));
    }
}

package cn.edu.hebau.aitravel.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/** 路线记录表对应的数据；也用于接收 AI 生成和保存路线的请求。 */
@Data
public class TravelRoute {
    private Long id;

    /** 由后端从令牌中取得，既不从请求读取，也不返回前端。 */
    @JsonIgnore
    private Long userId;

    private String destination;
    private Integer days;
    private String preference;
    private String routeContent;
    private String tipsContent;
    private Integer isCollect;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

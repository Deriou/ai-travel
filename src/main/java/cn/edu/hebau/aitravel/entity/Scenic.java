package cn.edu.hebau.aitravel.entity;

import lombok.Data;

/** 景点表对应的数据。 */
@Data
public class Scenic {
    private Long id;
    private Long cityId;
    private String scenicName;
    private String scenicDesc;
}

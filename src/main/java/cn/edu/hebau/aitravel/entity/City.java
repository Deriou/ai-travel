package cn.edu.hebau.aitravel.entity;

import lombok.Data;

/** 城市表对应的数据。 */
@Data
public class City {
    private Long id;
    private String cityName;
    private String description;
}

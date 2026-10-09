package cn.edu.hebau.aitravel.mapper;

import cn.edu.hebau.aitravel.entity.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 使用 SQL 查询城市表。 */
@Mapper
public interface CityMapper {
    @Select("SELECT id, city_name, description FROM city ORDER BY id")
    List<City> findAll();
}

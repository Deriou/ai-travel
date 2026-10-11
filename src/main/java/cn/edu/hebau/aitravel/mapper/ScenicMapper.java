package cn.edu.hebau.aitravel.mapper;

import cn.edu.hebau.aitravel.entity.Scenic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 使用 SQL 分页查询景点表；cityId 为空时不拼接 WHERE 条件，即查询全部景点。 */
@Mapper
public interface ScenicMapper {
    @Select("""
            <script>
            SELECT id, city_id, scenic_name, scenic_desc FROM scenic
            <if test="cityId != null">WHERE city_id = #{cityId}</if>
            ORDER BY id
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<Scenic> findPage(@Param("cityId") Long cityId,
                          @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    @Select("""
            <script>
            SELECT COUNT(*) FROM scenic
            <if test="cityId != null">WHERE city_id = #{cityId}</if>
            </script>
            """)
    long count(@Param("cityId") Long cityId);
}

package cn.edu.hebau.aitravel.mapper;

import cn.edu.hebau.aitravel.entity.TravelRoute;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 使用 SQL 操作路线记录表。
 * 删除和收藏的 WHERE 条件同时包含 id 和 user_id：路线不属于当前用户时匹配不到记录，返回 0。
 */
@Mapper
public interface RouteMapper {
    /** 新增后把数据库生成的自增编号写回 route.id。 */
    @Insert("""
            INSERT INTO travel_route
                (user_id, destination, days, preference, route_content, tips_content, is_collect, create_time)
            VALUES
                (#{userId}, #{destination}, #{days}, #{preference}, #{routeContent}, #{tipsContent}, 0, NOW())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TravelRoute route);

    @Select("""
            SELECT id, destination, days, preference, route_content, tips_content, is_collect, create_time
            FROM travel_route
            WHERE user_id = #{userId}
            ORDER BY create_time DESC, id DESC
            """)
    List<TravelRoute> findByUserId(Long userId);

    /** 返回删除的行数。 */
    @Delete("DELETE FROM travel_route WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    /** 返回匹配到的行数；MySQL 驱动默认按匹配行计数，重复设置相同状态也返回 1。 */
    @Update("UPDATE travel_route SET is_collect = #{isCollect} WHERE id = #{id} AND user_id = #{userId}")
    int updateCollect(@Param("id") Long id, @Param("userId") Long userId, @Param("isCollect") Integer isCollect);
}

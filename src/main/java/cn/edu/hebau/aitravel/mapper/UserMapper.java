package cn.edu.hebau.aitravel.mapper;

import cn.edu.hebau.aitravel.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** 使用 SQL 查询和新增用户；user 是 SQL 关键字，表名用反引号包裹。 */
@Mapper
public interface UserMapper {
    @Select("SELECT id, username, password, create_time FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO `user` (username, password, create_time) VALUES (#{username}, #{password}, NOW())")
    void insert(User user);
}

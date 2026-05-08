package com.doc.system.mapper;

import com.doc.system.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    @Select("select * from user where username = #{username}")
    User findByUsername(String username);

    // 添加插入用户的方法
    @Insert("insert into user(username, password) values(#{username}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
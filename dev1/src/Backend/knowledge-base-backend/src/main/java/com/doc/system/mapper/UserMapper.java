package com.doc.system.mapper;

import com.doc.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    User findById(Long id);
    int insert(User user);
    List<User> findAll();
    int updateRole(Long id, String role);
    int updateStatus(Long id, Integer status);
    int deleteById(Long id);
}

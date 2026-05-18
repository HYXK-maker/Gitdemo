package com.doc.system.mapper;

import com.doc.system.entity.Directory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DirectoryMapper {
    List<Directory> findAll();
    List<Directory> findByUserId(Long userId);
    Directory findById(Long id);
    List<Directory> findByParentId(Long parentId);
    int insert(Directory dir);
    int updateName(Long id, String name);
    int deleteById(Long id);
}

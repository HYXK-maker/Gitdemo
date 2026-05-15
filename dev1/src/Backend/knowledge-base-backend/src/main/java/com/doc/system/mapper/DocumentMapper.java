package com.doc.system.mapper;

import com.doc.system.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface DocumentMapper {
    List<Document> findAll();
    List<Document> findByUserId(Long userId);
    List<Document> findByDirectoryId(Long directoryId);
    Document findById(Long id);
    int insert(Document doc);

    int updateContent(@Param("id") Long id, @Param("content") String content);

    int updateTitle(@Param("id") Long id, @Param("title") String title);

    int deleteById(Long id);
}
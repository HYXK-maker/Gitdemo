package com.doc.system.mapper;

import com.doc.system.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DocumentMapper {
    List<Document> findAll();
    List<Document> findByUserId(Long userId);
    List<Document> findByDirectoryId(Long directoryId);
    Document findById(Long id);
    int insert(Document doc);
    int updateContent(Long id, String content);
    int updateTitle(Long id, String title);
    int deleteById(Long id);
}

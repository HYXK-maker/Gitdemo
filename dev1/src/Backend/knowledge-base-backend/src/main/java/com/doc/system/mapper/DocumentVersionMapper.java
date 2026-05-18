package com.doc.system.mapper;

import com.doc.system.entity.DocumentVersion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DocumentVersionMapper {

    @Select("SELECT * FROM document_version WHERE doc_id = #{docId} ORDER BY version_num ASC")
    List<DocumentVersion> findByDocId(Long docId);

    @Select("SELECT * FROM document_version WHERE id = #{id}")
    DocumentVersion findById(Long id);

    @Select("SELECT MAX(version_num) FROM document_version WHERE doc_id = #{docId}")
    Integer getMaxVersionNum(Long docId);

    @Insert("INSERT INTO document_version (doc_id, content, version_num, operator_id, version_note, create_time) " +
            "VALUES (#{docId}, #{content}, #{versionNum}, #{operatorId}, #{versionNote}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DocumentVersion version);

    @Delete("DELETE FROM document_version WHERE id = #{id}")
    int deleteById(Long id);
}

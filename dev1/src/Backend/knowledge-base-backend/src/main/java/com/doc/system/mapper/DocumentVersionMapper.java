package com.doc.system.mapper;

import com.doc.system.entity.DocumentVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentVersionMapper {

    @Select("SELECT * FROM document_version WHERE doc_id = #{docId} ORDER BY version_num DESC")
    List<DocumentVersion> findByDocId(Long docId);

    @Select("SELECT * FROM document_version WHERE id = #{id}")
    DocumentVersion findById(Long id);

    @Select("SELECT MAX(version_num) FROM document_version WHERE doc_id = #{docId}")
    Integer getMaxVersionNum(Long docId);

    @Insert("INSERT INTO document_version (doc_id, content, version_num, operator_id, version_note) VALUES (#{docId}, #{content}, #{versionNum}, #{operatorId}, #{versionNote})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DocumentVersion version);
}
package com.doc.system.service;

import com.doc.system.entity.Document;
import com.doc.system.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    public Document getById(Long id) {
        return documentMapper.findById(id);
    }

    public List<Document> getByDirectoryId(Long directoryId) {
        return documentMapper.findByDirectoryId(directoryId == null ? 0L : directoryId);
    }

    /** 创建文档，并指定所属用户 */
    public Document create(String title, String content, Long directoryId, Long userId) {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setContent(content == null ? "" : content);
        doc.setDirectoryId(directoryId == null ? 0L : directoryId);
        doc.setUserId(userId);
        documentMapper.insert(doc);
        return doc;
    }

    public void updateContent(Long id, String content) {
        documentMapper.updateContent(id, content);
    }

    public void rename(Long id, String title) {
        documentMapper.updateTitle(id, title);
    }

    public void delete(Long id) {
        documentMapper.deleteById(id);
    }
}

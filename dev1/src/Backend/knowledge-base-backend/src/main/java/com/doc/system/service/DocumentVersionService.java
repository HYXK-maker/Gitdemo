package com.doc.system.service;

import com.doc.system.entity.DocumentVersion;
import com.doc.system.mapper.DocumentVersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentVersionService {

    @Autowired
    private DocumentVersionMapper versionMapper;

    public List<DocumentVersion> getVersionsByDocId(Long docId) {
        return versionMapper.findByDocId(docId);
    }

    public DocumentVersion getVersionById(Long id) {
        return versionMapper.findById(id);
    }

    public DocumentVersion createVersion(Long docId, String content, Long operatorId, String versionNote) {
        Integer maxVersion = versionMapper.getMaxVersionNum(docId);
        int nextVersion = (maxVersion == null) ? 1 : maxVersion + 1;

        DocumentVersion version = new DocumentVersion();
        version.setDocId(docId);
        version.setContent(content);
        version.setVersionNum(nextVersion);
        version.setOperatorId(operatorId);
        version.setVersionNote(versionNote == null ? "" : versionNote);
        versionMapper.insert(version);
        return version;
    }
}
package com.doc.system.controller;

import com.doc.system.entity.Document;
import com.doc.system.entity.DocumentVersion;
import com.doc.system.service.DocumentService;
import com.doc.system.service.DocumentVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doc")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentVersionService versionService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null) {
            return Long.valueOf(userIdStr);
        }
        // 没有传用户ID时，默认返回1（admin）
        return 1L;
    }

    @GetMapping("/{id}")
    public Document getDoc(@PathVariable Long id) {
        return documentService.getById(id);
    }

    @PostMapping("/create")
    public Map<String, Object> createDoc(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        Long folderId = null;
        if (params.get("folderId") != null) {
            folderId = Long.valueOf(params.get("folderId").toString());
        }
        Document doc = documentService.create(title, content, folderId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", doc.getId());
        result.put("title", doc.getTitle());
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> updateDoc(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String title = (String) params.get("title");
        String content = (String) params.get("content");

        if (title != null) {
            documentService.rename(id, title);
        }
        if (content != null) {
            documentService.updateContent(id, content);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @PostMapping("/save")
    public String saveContent(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String content = (String) params.get("content");
        documentService.updateContent(id, content);
        return "ok";
    }

    @PostMapping("/rename")
    public String renameDoc(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        String title = params.get("title");
        documentService.rename(id, title);
        return "ok";
    }

    @PostMapping("/delete")
    public String deleteDoc(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        documentService.delete(id);
        return "ok";
    }

    // ========== 版本接口 ==========

    @GetMapping("/{docId}/versions")
    public List<DocumentVersion> getVersions(@PathVariable Long docId) {
        return versionService.getVersionsByDocId(docId);
    }

    @GetMapping("/versions/{versionId}")
    public DocumentVersion getVersionContent(@PathVariable Long versionId) {
        return versionService.getVersionById(versionId);
    }

    @PostMapping("/{docId}/version")
    public DocumentVersion createVersion(@PathVariable Long docId, @RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String content = params.get("content");
        String versionNote = params.get("versionNote");
        return versionService.createVersion(docId, content, userId, versionNote);
    }
    @DeleteMapping("/versions/{versionId}")
    public Map<String, Object> deleteVersion(@PathVariable Long versionId) {
        versionService.deleteVersion(versionId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
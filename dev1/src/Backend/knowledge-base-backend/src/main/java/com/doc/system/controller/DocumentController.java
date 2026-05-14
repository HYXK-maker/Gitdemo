package com.doc.system.controller;

import com.doc.system.entity.Document;
import com.doc.system.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/doc")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/{id}")
    public Document getDoc(@PathVariable Long id) {
        return documentService.getById(id);
    }

    @PostMapping("/create")
    public Map<String, Object> createDoc(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        Long folderId = null;
        if (params.get("folderId") != null) {
            folderId = Long.valueOf(params.get("folderId").toString());
        }
        Document doc = documentService.create(title, content, folderId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", doc.getId());
        result.put("title", doc.getTitle());
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
}

package com.doc.system.controller;

import com.doc.system.entity.Directory;
import com.doc.system.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dir")
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    @GetMapping("/tree")
    public List<Directory> getTree() {
        return directoryService.getTree();
    }

    @PostMapping("/create")
    public Map<String, Object> createDir(@RequestBody Map<String, Object> params) {
        String name = (String) params.get("name");
        Long parentId = params.get("parentId") != null ?
                Long.valueOf(params.get("parentId").toString()) : 0L;
        Directory dir = directoryService.createDir(name, parentId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", dir.getId());
        result.put("name", dir.getName());
        result.put("parentId", dir.getParentId());
        result.put("type", dir.getType());
        return result;
    }

    @PostMapping("/rename")
    public String renameDir(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        String name = params.get("name");
        directoryService.rename(id, name);
        return "ok";
    }

    @PostMapping("/delete")
    public String deleteDir(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        directoryService.deleteDir(id);
        return "ok";
    }
}

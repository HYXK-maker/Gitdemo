package com.doc.system.controller;

import com.doc.system.entity.Directory;
import com.doc.system.entity.Document;
import com.doc.system.service.DirectoryService;
import com.doc.system.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dir")
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private DocumentMapper documentMapper;

    /** 获取包含目录和文档的完整树 */
    @GetMapping("/tree")
    public List<Map<String, Object>> getTree() {
        List<Directory> dirs = directoryService.getTree();
        List<Document> docs = documentMapper.findAll();

        // 将目录转为 Map 列表（便于前端统一渲染）
        List<Map<String, Object>> tree = dirs.stream().map(dir -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", dir.getId());
            node.put("name", dir.getName());
            node.put("type", "dir");
            node.put("parentId", dir.getParentId());
            node.put("children", dir.getChildren() != null ?
                    dir.getChildren().stream().map(this::dirToMap).collect(Collectors.toList()) : new ArrayList<>());
            return node;
        }).collect(Collectors.toList());

        // 将每个文档挂载到对应的目录节点下（包括根目录）
        for (Document doc : docs) {
            Map<String, Object> docNode = new HashMap<>();
            docNode.put("id", doc.getId());
            docNode.put("name", doc.getTitle());
            docNode.put("type", "doc");
            docNode.put("docId", doc.getId());
            docNode.put("parentId", doc.getDirectoryId());

            // 找到父节点并添加
            Long parentId = doc.getDirectoryId();
            if (parentId == 0) {
                tree.add(docNode);
            } else {
                addToParent(tree, parentId, docNode);
            }
        }

        return tree;
    }

    private Map<String, Object> dirToMap(Directory dir) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", dir.getId());
        node.put("name", dir.getName());
        node.put("type", "dir");
        node.put("parentId", dir.getParentId());
        node.put("children", dir.getChildren() != null ?
                dir.getChildren().stream().map(this::dirToMap).collect(Collectors.toList()) : new ArrayList<>());
        return node;
    }

    private void addToParent(List<Map<String, Object>> nodes, Long parentId, Map<String, Object> child) {
        for (Map<String, Object> node : nodes) {
            if (node.get("id").equals(parentId)) {
                ((List<Map<String, Object>>) node.get("children")).add(child);
                return;
            }
            if (node.get("children") != null) {
                addToParent((List<Map<String, Object>>) node.get("children"), parentId, child);
            }
        }
    }

    /** POST /api/dir/create  body: { name, parentId } */
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

    /** POST /api/dir/rename  body: { id, name } */
    @PostMapping("/rename")
    public String renameDir(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        String name = params.get("name");
        directoryService.rename(id, name);
        return "ok";
    }

    /** POST /api/dir/delete  body: { id } */
    @PostMapping("/delete")
    public String deleteDir(@RequestBody Map<String, String> params) {
        Long id = Long.valueOf(params.get("id"));
        directoryService.deleteDir(id);
        return "ok";
    }
}

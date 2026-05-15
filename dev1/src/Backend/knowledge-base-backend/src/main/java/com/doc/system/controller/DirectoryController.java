package com.doc.system.controller;

import com.doc.system.entity.Directory;
import com.doc.system.entity.Document;
import com.doc.system.service.DirectoryService;
import com.doc.system.mapper.DirectoryMapper;
import com.doc.system.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/dir")
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private DirectoryMapper directoryMapper;

    @Autowired
    private DocumentMapper documentMapper;

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null) {
            return Long.valueOf(userIdStr);
        }
        return null;
    }

    @GetMapping("/tree")
    public List<Map<String, Object>> getTree(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        // 获取已构建好的目录树（根节点列表，含嵌套 children）
        List<Directory> dirs = (userId != null)
                ? directoryService.getTreeByUser(userId)
                : directoryService.getTreeByUser(null);  // 或查全部

        // 查文档
        List<Document> docs = (userId != null)
                ? documentMapper.findByUserId(userId)
                : documentMapper.findAll();

        // 递归转换目录树为前端需要的 Map 结构
        List<Map<String, Object>> tree = convertDirectoryTree(dirs);

        // 挂载文档
        Map<Long, Map<String, Object>> dirMap = buildDirMapRecursive(tree); // 递归收集所有目录节点
        for (Document doc : docs) {
            Map<String, Object> docNode = new HashMap<>();
            docNode.put("id", doc.getId());
            docNode.put("name", doc.getTitle());
            docNode.put("type", "doc");
            docNode.put("docId", doc.getId());
            docNode.put("parentId", doc.getDirectoryId());

            Long parentId = doc.getDirectoryId();
            if (parentId == null || parentId == 0) {
                tree.add(docNode);
            } else {
                Map<String, Object> parent = dirMap.get(parentId);
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(docNode);
                } else {
                    tree.add(docNode);
                }
            }
        }

        return tree;
    }

    // 递归将 Directory 树转换为 Map 树
    private List<Map<String, Object>> convertDirectoryTree(List<Directory> dirList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Directory dir : dirList) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", dir.getId());
            node.put("name", dir.getName());
            node.put("type", "dir");
            node.put("parentId", dir.getParentId());
            List<Map<String, Object>> children = convertDirectoryTree(dir.getChildren());
            node.put("children", children);
            result.add(node);
        }
        return result;
    }

    // 递归构建全量 dirMap（用于文档挂载）
    private Map<Long, Map<String, Object>> buildDirMapRecursive(List<Map<String, Object>> nodes) {
        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes) {
            map.put((Long) node.get("id"), node);
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children != null) {
                map.putAll(buildDirMapRecursive(children));
            }
        }
        return map;
    }
    @PostMapping("/create")
    public Map<String, Object> createDir(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String name = (String) params.get("name");
        Long parentId = params.get("parentId") != null ?
                Long.valueOf(params.get("parentId").toString()) : 0L;
        Directory dir = directoryService.createDir(name, parentId, userId);
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
package com.doc.system.service;

import com.doc.system.entity.Directory;
import com.doc.system.mapper.DirectoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    @Autowired
    private DirectoryMapper directoryMapper;

    /** 获取指定用户的目录树 */
    public List<Directory> getTreeByUser(Long userId) {
        List<Directory> all = directoryMapper.findByUserId(userId);
        return buildTree(all, 0L);
    }

    private List<Directory> buildTree(List<Directory> all, Long parentId) {
        return all.stream()
                .filter(d -> d.getParentId().equals(parentId))
                .peek(d -> d.setChildren(buildTree(all, d.getId())))
                .collect(Collectors.toList());
    }

    /** 创建目录，并指定所属用户 */
    public Directory createDir(String name, Long parentId, Long userId) {
        Directory dir = new Directory();
        dir.setName(name);
        dir.setParentId(parentId == null ? 0L : parentId);
        dir.setType("dir");
        dir.setUserId(userId);
        directoryMapper.insert(dir);
        return dir;
    }

    public void rename(Long id, String name) {
        directoryMapper.updateName(id, name);
    }

    @Transactional
    public void deleteDir(Long id) {
        deleteRecursive(id);
    }

    private void deleteRecursive(Long parentId) {
        List<Directory> children = directoryMapper.findByParentId(parentId);
        for (Directory child : children) {
            deleteRecursive(child.getId());
        }
        directoryMapper.deleteById(parentId);
    }
}

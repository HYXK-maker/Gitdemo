package com.doc.system.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Directory {
    private Long id;
    private String name;
    private Long parentId;
    private String type;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Directory> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<Directory> getChildren() { return children; }
    public void setChildren(List<Directory> children) { this.children = children; }
}

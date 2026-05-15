package com.doc.system.entity;

public class DocumentVersion {
    private Long id;
    private Long docId;
    private String content;
    private Integer versionNum;
    private Long operatorId;
    private String createTime;
    private String versionNote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getVersionNum() { return versionNum; }
    public void setVersionNum(Integer versionNum) { this.versionNum = versionNum; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getVersionNote() { return versionNote; }
    public void setVersionNote(String versionNote) { this.versionNote = versionNote; }
}
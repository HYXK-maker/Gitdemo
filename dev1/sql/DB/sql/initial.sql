create database if not exists doc_system default charset utf8mb4;
use doc_system;
-- 用户表
CREATE TABLE `user` (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '用户昵称',
    email VARCHAR(100) COMMENT '用户邮箱',
    avatar VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    phone VARCHAR(30) DEFAULT '' COMMENT '手机号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 文档目录表
CREATE TABLE directory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '目录ID',
    name VARCHAR(100) NOT NULL COMMENT '目录名称',
    type VARCHAR(20) DEFAULT 'folder' COMMENT '类型: folder=目录, file=文档',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    user_id BIGINT NOT NULL COMMENT '创建人ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父目录ID, 0=根目录',
    path VARCHAR(500) NOT NULL DEFAULT '' COMMENT '目录路径',
    level TINYINT NOT NULL DEFAULT 0 COMMENT '目录层级',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_path (path(255)),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档目录表';

-- 文档主表
CREATE TABLE document (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    content LONGTEXT COMMENT '文档内容',
    user_id BIGINT NOT NULL COMMENT '创建人ID',
    last_edit_user_id BIGINT COMMENT '最后编辑人ID',
    directory_id BIGINT NOT NULL COMMENT '所属目录ID',
    doc_type TINYINT NOT NULL DEFAULT 1 COMMENT '文档类型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0未删除 1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_directory_id (directory_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档主表';

-- 文档版本表
CREATE TABLE document_version (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '版本ID',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    content LONGTEXT NOT NULL COMMENT '版本内容',
    version_num INT NOT NULL COMMENT '版本号',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本生成时间',
    version_note VARCHAR(200) DEFAULT '' COMMENT '版本变更说明',
    PRIMARY KEY (id),
    UNIQUE KEY uk_doc_version (doc_id, version_num),
    KEY idx_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本表';

-- 插入默认管理员
INSERT INTO `user` (username, password, nickname) VALUES ('admin', '123456', '管理员');
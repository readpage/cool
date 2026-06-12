package com.example.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体 — 对应 role 表
 */
@Data
@TableName("role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 角色名（英文标识） */
    private String name;

    /** 角色昵称（中文展示） */
    private String nickname;

    /** test */
    private String test;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

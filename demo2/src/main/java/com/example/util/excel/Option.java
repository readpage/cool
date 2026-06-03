package com.example.util.excel;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String type;

    private String label;

    private String value;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

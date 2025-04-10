package com.undraw.domain.entity;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("role")
@ToString
@Schema(title = "role")
public class Role implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(title = "角色ID")
	@ExcelIgnore
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	@Schema(title = "角色名")
	@ExcelProperty(value = "角色名")
	private String name;

	@Schema(title = "角色昵称")
	@ExcelProperty(value = "角色昵称")
	private String nickname;

	@Schema(title = "test")
	@ExcelProperty(value = "test")
	private String test;

	@Schema(title = "创建时间")
	@ExcelIgnore
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@Schema(title = "修改时间")
	@ExcelIgnore
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;


}
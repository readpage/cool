package com.example.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(title = "选项")
public class Model {

    @Schema(title = "标签")
    private String label;

    @Schema(title = "值")
    private String value;

}

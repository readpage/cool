package com.undraw.util.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(title = "分页参数")
public class PageParam {

    @Schema(title = "页数")
    private Integer current;

    @Schema(title = "页大小")
    private Integer size;

}
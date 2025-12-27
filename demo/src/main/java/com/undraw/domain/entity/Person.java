package com.undraw.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
/**
 * 指定集合的名称如果不指定
 * 默认为当前类名小写
 */
@Document(collection = "person")

public class Person {
    @Id
    private Long id;

    private String name;

    private Integer age;

}

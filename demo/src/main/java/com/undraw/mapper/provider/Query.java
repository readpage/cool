package com.undraw.mapper.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Query {

    private List<String> fieldNames;

    private Map<String, Object> data;


}

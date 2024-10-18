package com.undraw.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Model {

    private String label;

    @JsonProperty("value1")
    private String value;
}

package com.stock.mockstock.domain.stock.dto.kis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KisOrderbookResponse {

    @JsonProperty("rt_cd")
    private String rtCd;

    private String msg1;

    @JsonProperty("output1")
    private Map<String, String> output1;

    @JsonProperty("output2")
    private Map<String, String> output2;
}
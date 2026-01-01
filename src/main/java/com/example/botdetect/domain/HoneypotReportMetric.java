package com.example.botdetect.domain;

import lombok.*;

import java.util.Map;


@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class HoneypotReportMetric {
    private long timestamp;
    private String nonce;
    private String userAgent;
    private Map<String, Object> result;
    private String integrityHash;

}

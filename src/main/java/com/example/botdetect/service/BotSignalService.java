package com.example.botdetect.service;

import com.example.botdetect.domain.HoneypotReportMetric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BotSignalService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> usedNonces = ConcurrentHashMap.newKeySet();

    public boolean isNonReplayRequest(HoneypotReportMetric report) {
        return !usedNonces.add(report.getNonce());
    }

    public boolean verifyRequestIntegrity(HoneypotReportMetric reportMetric) throws Exception {
        String canonical = canonicalString(reportMetric);
        String computedHash = sha256Hex(canonical);
        return computedHash.equalsIgnoreCase(reportMetric.getIntegrityHash());
    }

    private String canonicalString(HoneypotReportMetric r) throws Exception {
        return String.join("|",
                String.valueOf(r.getTimestamp()),
                r.getNonce(),
                r.getUserAgent(),
                mapper.writeValueAsString(r.getResult())
        );
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}

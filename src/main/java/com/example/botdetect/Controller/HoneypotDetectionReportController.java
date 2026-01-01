package com.example.botdetect.Controller;

import com.example.botdetect.domain.HoneypotReportMetric;
import com.example.botdetect.service.BotSignalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HoneypotDetectionReportController {

    private static final Logger logger = LoggerFactory.getLogger(HoneypotDetectionReportController.class);

    private final BotSignalService botSignalService;

    @Autowired
    public HoneypotDetectionReportController(BotSignalService botSignalService) {
        this.botSignalService = botSignalService;
    }


//    @PostMapping("/honeypot/report")
//    public ResponseEntity<?> receiveReport(@RequestBody HoneypotReport report) {
//        // Log, store, alert, or forward to SIEM
//        System.out.println("Suspicious activity detected: " + report);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/honeypot/report")
    public ResponseEntity<String> receive(@RequestBody HoneypotReportMetric report) throws Exception {

        if (botSignalService.isNonReplayRequest(report)) {
            return ResponseEntity.ok("Replay ignored");
        }


        if(!botSignalService.verifyRequestIntegrity(report)) {
            return ResponseEntity.badRequest().body("Integrity check failed");
        }

        logger.warn("bot activity detected: {}", report.getResult());
        return ResponseEntity.ok("OK");
    }
}

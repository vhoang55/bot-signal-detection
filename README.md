# bot-signal-detection
proof of concept for detect bot signal in client


To run the application, use the following Maven command:

```bash
mvn spring-boot:run

or just import the maven project into your favorite IDE and run the main application class `com.example.botdetect.BotdetectApplication`
```

navigate to `http://localhost:8080` to access the application.

## Documentation and Features

At root level, it rediect to `http://localhost:8080/welcome.html`
with a sign up link with a form. The form uses `/js/minifydetector.js`
to analyze the form submission to detect a signal of bot activity.

run the analyzeForm to check if the form was submitted by a bot:
1) Any of the honeypot form field was filled out
2) Was the form submit to fast
3) was there any interaction with the mouse or keyboard

if any of the condition above is met, report a signal of bot activity.

```javascript
  document
      .getElementById('registrationForm')
      .addEventListener('submit', (event) => {
        const result = detector.analyzeForm(event.target);
        detector.report(result);
      });
```

which then submit a signal to the server

`http://localhost:8080/api/honeypot/report`

with sample json payload

```json
{
  "timestamp": 1767294782350,
  "nonce": "1b1ac1ac-0e43-4434-96ed-5fe54ff6833c",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36",
  "result": {
    "suspicious": true,
    "reasons": [
      "HONEYPOT_FIELD_FILLED"
    ],
    "elapsed": 33332,
    "interactions": {
      "mouse": true,
      "keyboard": true,
      "focus": true
    }
  },
  "integrityHash": "27edd7a99e06e7ce235f058ef37f42b303dda4c7f20ee59a3730f0ae99ac6f96"
}
```

Server side when receiving the report, it will validate the integrityHash to ensure the report is not tampered with.


```java

    if(!botSignalService.verifyRequestIntegrity(report)) {
        return ResponseEntity.badRequest().body("Integrity check failed");
    }

```

```java

 public boolean verifyRequestIntegrity(HoneypotReportMetric reportMetric) throws Exception {
        String canonical = canonicalString(reportMetric);
        String computedHash = sha256Hex(canonical);
        return computedHash.equalsIgnoreCase(reportMetric.getIntegrityHash());
    }
```
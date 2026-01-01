package com.example.botdetect.Controller;



import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller

public class RegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @PostMapping("/register")
    public String handleRegistration(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "company_fax", required = false) String companyFax,
            @RequestParam(value = "address_line_3", required = false) String addressLine3,
            RedirectAttributes redirectAttrs) {

        boolean honeypotTriggered = !StringUtils.isBlank(companyFax) || !StringUtils.isBlank(addressLine3);

        if (honeypotTriggered) {
            logger.warn("Honeypot triggered - dropping submission: firstName='{}' lastName='{}' company_fax='{}' address_line_3='{}'",
                    firstName, lastName, companyFax, addressLine3);
        }

        logger.info("New registration: firstName='{}' lastName='{}' message='{}'", firstName, lastName, message);

        return "redirect:/confirmation.html";
    }

}
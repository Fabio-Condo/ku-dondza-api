package com.fabiocondo.payments.controller;

import com.fabiocondo.payments.service.EMolaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emola")
public class EMolaController {

    private final EMolaService eMolaService;

    public EMolaController(EMolaService eMolaService) {
        this.eMolaService = eMolaService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestParam String phoneNumber, @RequestParam double amount) {
        try {
            String response = eMolaService.transferMoney(phoneNumber, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}

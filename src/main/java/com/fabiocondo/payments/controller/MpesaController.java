package com.fabiocondo.payments.controller;

import com.fabiocondo.payments.service.MpesaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mpesa")
public class MpesaController {

    private final MpesaService mpesaService;

    public MpesaController(MpesaService mpesaService) {
        this.mpesaService = mpesaService;
    }

    @GetMapping("/initiateC2BTransaction")
    public String initiateC2BTransaction(@RequestParam String phoneNumber, @RequestParam double amount) {
        try {
            String accessToken = mpesaService.getAccessToken();
            return mpesaService.makeC2BTransaction(accessToken, phoneNumber, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}


package com.fabiocondo.service.impl;

import com.fabiocondo.enumeration.Plan;
import org.springframework.stereotype.Service;

@Service
public class MPesaPaymentService {

    public boolean simulateMpesaPayment(String phoneNumber, Plan plan) {
        System.out.println("Simulando pagamento MPESA para " + phoneNumber +
                " no plano " + plan + "...");
        simulateNetworkDelay();
        // Lógica fictícia de sucesso (exemplo: sempre dá certo se começa com 84 ou 85)
        return phoneNumber.startsWith("84") || phoneNumber.startsWith("85");
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1500); // simula um pequeno atraso da transação
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package com.fabiocondo.service.impl;

import com.fabiocondo.enumeration.Plan;
import org.springframework.stereotype.Service;

@Service
public class EmolaPaymentService {

    public boolean simulateEmolaPayment(String phoneNumber, Plan plan) {
        System.out.println("Simulando pagamento EMOLA para " + phoneNumber +
                " no plano " + plan + "...");
        simulateNetworkDelay();
        // Lógica fictícia de sucesso (exemplo: sempre dá certo se começa com 86 ou 87)
        return phoneNumber.startsWith("86") || phoneNumber.startsWith("87");
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1500); // simula um pequeno atraso da transação
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

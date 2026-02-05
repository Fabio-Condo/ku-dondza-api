package com.fabiocondo.payments.emola;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class EmolaPaymentController {

    private final EmolaService emolaService;

    public EmolaPaymentController(EmolaService emolaService) {
        this.emolaService = emolaService;
    }

    @PostMapping("/emola")
    public String pay(@RequestParam String msisdn,
                      @RequestParam String amount) {

        return emolaService.pushUssd(
                msisdn,
                amount,
                "Pagamento do serviço",
                "PED123"
        );
    }
}

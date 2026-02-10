package bot.core.kassa;

import bot.core.kassa.DTO.Amount;
import bot.core.kassa.DTO.Confirmation;
import bot.core.kassa.DTO.PaymentRequest;
import bot.core.util.config.ShopConfig;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentService {
    private final YooKassaClient client;

    public PaymentService() {
        client = new YooKassaClient(new ShopConfig());
    }

    public void sendPaymentRequest(BigDecimal cost, String returnUrl, String description) {
        sendPaymentRequest(buildPaymentRequest(cost, returnUrl, description));
    }

    private void sendPaymentRequest(PaymentRequest paymentRequest) {
        try {
            client.sendPaymentRequest(paymentRequest, UUID.randomUUID().toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PaymentRequest buildPaymentRequest(BigDecimal cost, String returnUrl, String description) {
        PaymentRequest request = new PaymentRequest();

        Amount amount = new Amount();
        amount.setValue(cost);
        amount.setCurrency("RUB");
        request.setAmount(amount);

        Confirmation confirmation = new Confirmation();
        confirmation.setType("redirect");
        confirmation.setReturnUrl(returnUrl);
        request.setConfirmation(confirmation);

        request.setCapture(true);
        request.setDescription(description);

        return request;
    }
}
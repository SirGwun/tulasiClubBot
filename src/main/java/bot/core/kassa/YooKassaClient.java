package bot.core.kassa;

import bot.core.kassa.DTO.PaymentRequest;
import bot.core.util.config.ShopConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class YooKassaClient {
    HttpClient client;
    ShopConfig config;

    ObjectMapper mapper;
    Logger logger = LoggerFactory.getLogger(YooKassaClient.class);

    public YooKassaClient(ShopConfig config) {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.config = config;

        mapper = new ObjectMapper();
    }

    public void sendPaymentRequest(PaymentRequest paymentRequest, String idempotenceKey) throws JsonProcessingException {
        String encodedAuth = getEncodedAuth();
        String jsonBody = mapper.writeValueAsString(paymentRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create("https://api.yookassa.ru/v3/payments"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .header("Idempotence-Key",  idempotenceKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int status = response.statusCode();

                    if (status >= 200 && status < 300) {
                        // OK
                        System.out.println("Response body");
                        System.out.println(response.body());
                    } else {
                        logger.error("Ответ от yookassa {}", status);
                    }

                })
                .exceptionally(ex -> {
                    logger.error("Ошибка при обработке ответа от сервера yookassa {}", ex.getMessage());
                    return null;
                });
    }

    private String getEncodedAuth() {
        String auth = config.getShopId() + ":" + config.getSecretKey();
        return Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }
}
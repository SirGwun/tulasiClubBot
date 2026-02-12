package bot.core.kassa;

import bot.core.kassa.DTO.PaymentRequest;
import bot.core.kassa.DTO.PaymentResponse;
import bot.core.kassa.excaptions.JsonMappingException;
import bot.core.kassa.excaptions.YooKassaAPIException;
import bot.core.util.config.ShopConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class YooKassaClient {
    private final HttpClient client;
    private final ShopConfig config;

    ObjectMapper mapper;
    Logger logger = LoggerFactory.getLogger(YooKassaClient.class);

    public YooKassaClient(ShopConfig config) {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.config = config;

        mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public CompletableFuture<PaymentResponse> sendPaymentRequest(PaymentRequest paymentRequest, String idempotenceKey)
            throws YooKassaAPIException {
        try {
            HttpRequest request = prepareRequest(paymentRequest, idempotenceKey);

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::handleResponse);

        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(
                    new JsonMappingException(e.getMessage())
            );
        }
    }

    private HttpRequest prepareRequest(PaymentRequest paymentRequest, String idempotenceKey) throws JsonProcessingException {
        String encodedAuth = getEncodedAuth();
        String jsonBody = mapper.writeValueAsString(paymentRequest);

        return HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(10))
                .uri(URI.create(config.getShopUrl()))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .header("Idempotence-Key",  idempotenceKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
    }

    private PaymentResponse handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status >= 200 && status < 300) {
            logger.debug("Response body");
            logger.debug(response.body());
            try {
                return mapper.readValue(response.body(), PaymentResponse.class);
            } catch (JsonProcessingException e) {
                throw new JsonMappingException("Failed to parse YooKassa response", e);
            }
        } else {
            logger.error("Ответ от YooKassa {} {}", status, response.body());
            throw new YooKassaAPIException(
                    "HTTP " + status + ": " + response.body(),
                    status
            );
        }
    }

    private String getEncodedAuth() {
        String auth = config.getShopId() + ":" + config.getSecretKey();
        return Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }
}
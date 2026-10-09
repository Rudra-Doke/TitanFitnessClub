package com.titan.service;

import com.titan.entity.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Service
public class RazorpayService {
    private static final String API_BASE = "https://api.razorpay.com/v1";

    private final String keyId;
    private final String keySecret;
    private final RestClient restClient;

    public RazorpayService(@Value("${razorpay.key-id:}") String keyId,
                           @Value("${razorpay.key-secret:}") String keySecret) {
        this.keyId = keyId == null ? "" : keyId.trim();
        this.keySecret = keySecret == null ? "" : keySecret.trim();
        this.restClient = RestClient.builder().baseUrl(API_BASE).build();
    }

    public boolean isConfigured() {
        return !keyId.isBlank() && !keySecret.isBlank();
    }

    public String getKeyId() {
        return keyId;
    }

    public String createOrder(Payment payment) {
        requireConfigured();
        long amountPaise = amountInPaise(payment.getAmount());
        Map<?, ?> result = restClient.post()
                .uri("/orders")
                .headers(this::addAuthorization)
                .body(Map.of("amount", amountPaise, "currency", "INR",
                        "receipt", "titan-payment-" + payment.getId(),
                        "notes", Map.of("payment_id", payment.getId().toString())))
                .retrieve()
                .body(Map.class);
        Object id = result == null ? null : result.get("id");
        if (!(id instanceof String orderId) || orderId.isBlank()) {
            throw new IllegalStateException("The payment provider did not return an order reference.");
        }
        return orderId;
    }

    public boolean verifyCapturedPayment(Payment payment, String orderId, String paymentId, String signature) {
        requireConfigured();
        if (payment == null || orderId == null || paymentId == null || signature == null
                || !orderId.equals(payment.getRazorpayOrderId()) || payment.getAmount() == null) {
            return false;
        }
        if (!isValidSignature(orderId, paymentId, signature, keySecret)) return false;

        Map<?, ?> providerPayment = restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .headers(this::addAuthorization)
                .retrieve()
                .body(Map.class);
        if (providerPayment == null) return false;
        return "captured".equals(providerPayment.get("status"))
                && orderId.equals(providerPayment.get("order_id"))
                && "INR".equals(providerPayment.get("currency"))
                && providerAmountMatches(providerPayment.get("amount"), payment.getAmount());
    }

    public static boolean isValidSignature(String orderId, String paymentId, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            byte[] supplied = HexFormat.of().parseHex(signature);
            return MessageDigest.isEqual(expected, supplied);
        } catch (IllegalArgumentException | java.security.GeneralSecurityException ex) {
            return false;
        }
    }

    public static long amountInPaise(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive.");
        }
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2).longValueExact();
    }

    private static boolean providerAmountMatches(Object providerAmount, Double expectedAmount) {
        if (!(providerAmount instanceof Number number)) return false;
        try {
            return number.longValue() == amountInPaise(expectedAmount);
        } catch (ArithmeticException | IllegalArgumentException ex) {
            return false;
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) throw new IllegalStateException("Online payments are not configured yet.");
    }

    private void addAuthorization(HttpHeaders headers) {
        headers.setBasicAuth(keyId, keySecret);
        headers.set("Content-Type", "application/json");
    }
}

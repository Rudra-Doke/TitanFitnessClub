package com.titan.service;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RazorpayServiceTest {
    @Test
    void convertsRupeesToPaiseWithoutFloatingPointDrift() {
        assertEquals(12345L, RazorpayService.amountInPaise(123.45));
    }

    @Test
    void validatesOnlyTheExpectedCheckoutSignature() throws Exception {
        String orderId = "order_test123";
        String paymentId = "pay_test123";
        String secret = "test-secret";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8)));

        assertTrue(RazorpayService.isValidSignature(orderId, paymentId, signature, secret));
        assertFalse(RazorpayService.isValidSignature(orderId, "pay_tampered", signature, secret));
        assertFalse(RazorpayService.isValidSignature(orderId, paymentId, "not-a-signature", secret));
    }
}

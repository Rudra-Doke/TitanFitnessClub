package com.titan.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.titan.entity.Payment;
import com.titan.repository.PaymentRepository;
import com.titan.service.PaymentService;
import com.titan.service.RazorpayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class RazorpayWebhookController {
    private final RazorpayService razorpayService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public RazorpayWebhookController(RazorpayService razorpayService,
                                     PaymentRepository paymentRepository,
                                     PaymentService paymentService,
                                     ObjectMapper objectMapper) {
        this.razorpayService = razorpayService;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhooks/razorpay")
    public ResponseEntity<Void> receive(@RequestBody byte[] body,
                                        @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        if (!razorpayService.isWebhookConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!razorpayService.verifyWebhookSignature(body, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final JsonNode event;
        try {
            event = objectMapper.readTree(body);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().build();
        }

        // Acknowledge valid events we do not act on; only captured payments grant membership.
        if (!"payment.captured".equals(event.path("event").asText())) {
            return ResponseEntity.ok().build();
        }

        JsonNode paymentNode = event.path("payload").path("payment").path("entity");
        String orderId = paymentNode.path("order_id").asText("");
        String providerPaymentId = paymentNode.path("id").asText("");
        if (orderId.isBlank() || providerPaymentId.isBlank()
                || !"captured".equals(paymentNode.path("status").asText())
                || !"INR".equals(paymentNode.path("currency").asText())) {
            return ResponseEntity.ok().build();
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId).orElse(null);
        if (payment == null || !providerAmountMatches(paymentNode.path("amount"), payment)) {
            return ResponseEntity.ok().build();
        }
        if ("Paid".equalsIgnoreCase(payment.getPaymentStatus())) {
            // Razorpay can retry a webhook; the same capture is already recorded.
            return ResponseEntity.ok().build();
        }
        if (!"Pending".equalsIgnoreCase(payment.getPaymentStatus())) {
            return ResponseEntity.ok().build();
        }

        payment.setRazorpayPaymentId(providerPaymentId);
        payment.setPaymentMethod("Razorpay");
        payment.setPaymentStatus("Paid");
        payment.setPaymentDate(LocalDate.now());
        paymentService.savePayment(payment);
        return ResponseEntity.ok().build();
    }

    private static boolean providerAmountMatches(JsonNode amountNode, Payment payment) {
        if (!amountNode.isIntegralNumber() || payment.getAmount() == null) return false;
        try {
            return amountNode.longValue() == RazorpayService.amountInPaise(payment.getAmount());
        } catch (ArithmeticException | IllegalArgumentException ex) {
            return false;
        }
    }
}

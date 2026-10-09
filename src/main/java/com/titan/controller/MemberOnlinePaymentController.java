package com.titan.controller;

import com.titan.entity.Member;
import com.titan.entity.Payment;
import com.titan.repository.MemberRepository;
import com.titan.repository.PaymentRepository;
import com.titan.service.PaymentService;
import com.titan.service.RazorpayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class MemberOnlinePaymentController {
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

    public MemberOnlinePaymentController(MemberRepository memberRepository,
                                         PaymentRepository paymentRepository,
                                         PaymentService paymentService,
                                         RazorpayService razorpayService) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
    }

    @PostMapping("/member/payments/{paymentId}/order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(@PathVariable Long paymentId,
                                                            Authentication authentication) {
        if (!razorpayService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Online payment is not configured yet. Please contact the gym."));
        }
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (member == null || payment == null || !payment.getMember().getId().equals(member.getId())) {
            return ResponseEntity.notFound().build();
        }
        if (!"Pending".equalsIgnoreCase(payment.getPaymentStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "This payment is no longer awaiting payment."));
        }
        try {
            String orderId = razorpayService.createOrder(payment);
            payment.setRazorpayOrderId(orderId);
            payment.setPaymentMethod("Razorpay");
            paymentService.savePayment(payment);
            return ResponseEntity.ok(Map.of("keyId", razorpayService.getKeyId(), "orderId", orderId,
                    "amount", RazorpayService.amountInPaise(payment.getAmount()), "currency", "INR",
                    "name", "Titan Fitness Club", "description", payment.getMembershipPlan().getPlanName() + " membership",
                    "prefill", Map.of("name", member.getFullName(),
                            "email", member.getEmail() == null ? "" : member.getEmail(),
                            "contact", member.getPhone() == null ? "" : member.getPhone())));
        } catch (RestClientException | IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Could not start checkout. Please try again or contact the gym."));
        }
    }

    @PostMapping("/member/payments/{paymentId}/verify")
    @ResponseBody
    public ResponseEntity<Map<String, String>> verifyPayment(@PathVariable Long paymentId,
                                                              @RequestParam("razorpay_order_id") String orderId,
                                                              @RequestParam("razorpay_payment_id") String razorpayPaymentId,
                                                              @RequestParam("razorpay_signature") String signature,
                                                              Authentication authentication) {
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (member == null || payment == null || !payment.getMember().getId().equals(member.getId())) {
            return ResponseEntity.notFound().build();
        }
        if ("Paid".equalsIgnoreCase(payment.getPaymentStatus())
                && razorpayPaymentId.equals(payment.getRazorpayPaymentId())) {
            return ResponseEntity.ok(Map.of("message", "Payment is confirmed."));
        }
        try {
            if (!razorpayService.verifyCapturedPayment(payment, orderId, razorpayPaymentId, signature)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment could not be verified. If you were charged, contact the gym."));
            }
            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setPaymentMethod("Razorpay");
            payment.setPaymentStatus("Paid");
            payment.setPaymentDate(LocalDate.now());
            paymentService.savePayment(payment);
            return ResponseEntity.ok(Map.of("message", "Payment confirmed. Your membership is updated."));
        } catch (RestClientException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "We could not confirm the payment yet. Please refresh in a moment or contact the gym."));
        }
    }
}

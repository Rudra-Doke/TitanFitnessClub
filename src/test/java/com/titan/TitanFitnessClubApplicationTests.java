package com.titan;

import com.titan.entity.Role;
import com.titan.entity.Member;
import com.titan.entity.MembershipPlan;
import com.titan.entity.Payment;
import com.titan.entity.Attendance;
import com.titan.repository.AttendanceRepository;
import com.titan.entity.User;
import com.titan.repository.MemberRepository;
import com.titan.repository.MembershipPlanRepository;
import com.titan.repository.PaymentRepository;
import com.titan.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class TitanFitnessClubApplicationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() { }

    @Test
    void memberCannotAccessAdminMemberList() throws Exception {
        mockMvc.perform(get("/members").with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCannotAccessMemberPrivatePages() throws Exception {
        mockMvc.perform(get("/member/profile").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void destructiveGetRouteIsNoLongerAvailable() throws Exception {
        mockMvc.perform(get("/members/delete/404").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deletePostRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/members/delete/404").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfProtectedDeletePostIsAccepted() throws Exception {
        mockMvc.perform(post("/members/delete/404").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void disabledAccountsAreNotReportedAsEnabled() {
        String username = "disabled-" + java.util.UUID.randomUUID();
        User account = new User();
        account.setUsername(username);
        account.setPassword("{noop}test-only-unused-password");
        account.setRole(Role.MEMBER);
        account.setEnabled(false);
        userRepository.saveAndFlush(account);

        assertFalse(userDetailsService.loadUserByUsername(username).isEnabled());
    }

    @Test
    @Transactional
    void revenueIncludesPaidTransactionsOnly() {
        double previousTotal = paymentRepository.getTotalRevenue();
        Member member = new Member();
        member.setMemberId("TEST" + java.util.UUID.randomUUID());
        member.setFullName("Test Member");
        member.setPhone("0000000000");
        memberRepository.saveAndFlush(member);

        MembershipPlan plan = new MembershipPlan();
        plan.setPlanName("Test Plan " + java.util.UUID.randomUUID());
        planRepository.saveAndFlush(plan);

        Payment paid = new Payment();
        paid.setMember(member);
        paid.setMembershipPlan(plan);
        paid.setAmount(50.0);
        paid.setPaymentStatus("PAID");
        paymentRepository.saveAndFlush(paid);

        Payment pending = new Payment();
        pending.setMember(member);
        pending.setMembershipPlan(plan);
        pending.setAmount(80.0);
        pending.setPaymentStatus("Pending");
        paymentRepository.saveAndFlush(pending);

        assertEquals(previousTotal + 50.0, paymentRepository.getTotalRevenue(), 0.001);
    }

    @Test
    @Transactional
    void paymentFormResolvesSelectedMemberAndPlanIds() throws Exception {
        Member member = new Member();
        member.setMemberId("PAY" + java.util.UUID.randomUUID());
        member.setFullName("Payment Form Member");
        member.setPhone("0000000000");
        memberRepository.saveAndFlush(member);

        MembershipPlan plan = new MembershipPlan();
        plan.setPlanName("Payment Form Plan " + java.util.UUID.randomUUID());
        planRepository.saveAndFlush(plan);

        mockMvc.perform(post("/payments/save")
                        .with(user("admin").roles("ADMIN")).with(csrf())
                        .param("memberId", member.getId().toString())
                        .param("membershipPlanId", plan.getId().toString())
                        .param("amount", "42.50")
                        .param("paymentDate", "2026-10-09")
                        .param("paymentMethod", "Cash")
                        .param("paymentStatus", "Paid"))
                .andExpect(status().is3xxRedirection());

        assertEquals(1, paymentRepository.findByMemberOrderByPaymentDateDesc(member).size());
    }

    @Test
    @Transactional
    void attendanceFormResolvesSelectedMemberId() throws Exception {
        Member member = new Member();
        member.setMemberId("ATT" + java.util.UUID.randomUUID());
        member.setFullName("Attendance Form Member");
        member.setPhone("0000000000");
        memberRepository.saveAndFlush(member);

        mockMvc.perform(post("/attendance/save")
                        .with(user("admin").roles("ADMIN")).with(csrf())
                        .param("memberId", member.getId().toString())
                        .param("attendanceDate", "2026-10-09")
                        .param("checkIn", "08:00")
                        .param("status", "Present"))
                .andExpect(status().is3xxRedirection());

        assertEquals(1, attendanceRepository.findByMemberOrderByAttendanceDateDesc(member).size());
    }

    @Test
    @Transactional
    void registrationCreatesAccountMemberAndPendingPaymentTogether() throws Exception {
        MembershipPlan plan = new MembershipPlan();
        plan.setPlanName("Registration Plan " + java.util.UUID.randomUUID());
        plan.setPrice(120.0);
        plan.setDurationMonths(1);
        plan.setStatus("Active");
        planRepository.saveAndFlush(plan);
        String username = "registered-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@example.test";

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Registered Member")
                        .param("username", username)
                        .param("email", email)
                        .param("phone", "1234567890")
                        .param("password", "secure-test-pass")
                        .param("confirmPassword", "secure-test-pass")
                        .param("age", "25")
                        .param("gender", "Female")
                        .param("fitnessGoal", "General Fitness")
                        .param("membershipPlan", plan.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertEquals(true, userRepository.existsByUsername(username));
        Member member = memberRepository.findByUsername(username).orElseThrow();
        var payments = paymentRepository.findByMemberOrderByPaymentDateDesc(member);
        assertEquals(1, payments.size());
        assertEquals("Pending", payments.get(0).getPaymentStatus());
        assertEquals("General Fitness", member.getFitnessGoal());
    }

    @Test
    @Transactional
    void memberCanUpdateOnlyTheirOwnProfileFields() throws Exception {
        String username = "profile-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member();
        member.setMemberId("PROFILE" + java.util.UUID.randomUUID());
        member.setFullName("Before Name");
        member.setPhone("1234567890");
        member.setUsername(username);
        member.setEmail(username + "@example.test");
        memberRepository.saveAndFlush(member);

        mockMvc.perform(post("/member/profile/update").with(user(username).roles("MEMBER")).with(csrf())
                        .param("fullName", "After Name")
                        .param("email", "after-" + username + "@example.test")
                        .param("phone", "9876543210")
                        .param("address", "Gym Street")
                        .param("fitnessGoal", "Strength"))
                .andExpect(status().is3xxRedirection());

        Member updated = memberRepository.findByUsername(username).orElseThrow();
        assertEquals("After Name", updated.getFullName());
        assertEquals("Gym Street", updated.getAddress());
    }

    @Test
    @Transactional
    void memberCanChangePasswordAfterVerifyingCurrentPassword() throws Exception {
        String username = "password-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        User account = new User();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode("current-password"));
        account.setRole(Role.MEMBER);
        userRepository.saveAndFlush(account);

        mockMvc.perform(post("/member/profile/password").with(user(username).roles("MEMBER")).with(csrf())
                        .param("currentPassword", "current-password")
                        .param("newPassword", "replacement-password")
                        .param("confirmPassword", "replacement-password"))
                .andExpect(status().is3xxRedirection());

        assertTrue(passwordEncoder.matches("replacement-password", userRepository.findByUsername(username).orElseThrow().getPassword()));
    }

    @Test
    @Transactional
    void memberCanRequestPlanRenewalWithoutMarkingItPaid() throws Exception {
        String username = "renewal-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member();
        member.setMemberId("RENEW" + java.util.UUID.randomUUID());
        member.setFullName("Renewal Member");
        member.setPhone("1234567890");
        member.setUsername(username);
        memberRepository.saveAndFlush(member);
        MembershipPlan plan = new MembershipPlan();
        plan.setPlanName("Renewal Plan " + java.util.UUID.randomUUID());
        plan.setPrice(100.0);
        plan.setDurationMonths(1);
        plan.setStatus("Active");
        planRepository.saveAndFlush(plan);

        mockMvc.perform(post("/member/membership/request").with(user(username).roles("MEMBER")).with(csrf())
                        .param("planId", plan.getId().toString()))
                .andExpect(status().is3xxRedirection());

        var payments = paymentRepository.findByMemberOrderByPaymentDateDesc(member);
        assertEquals(1, payments.size());
        assertEquals("Pending", payments.get(0).getPaymentStatus());
        assertEquals("Renewal Request", payments.get(0).getPaymentMethod());
    }

    @Test
    @Transactional
    void adminCanSearchMembersByNameOrId() throws Exception {
        String memberName = "Searchable " + java.util.UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member();
        member.setMemberId("SEARCH" + java.util.UUID.randomUUID());
        member.setFullName(memberName);
        member.setPhone("1234567890");
        memberRepository.saveAndFlush(member);

        mockMvc.perform(get("/members").with(user("admin").roles("ADMIN")).param("q", memberName))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(memberName)));
    }

    @Test
    @Transactional
    void memberDashboardRendersAccountSpecificSummary() throws Exception {
        String username = "dashboard-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member();
        member.setMemberId("DASH" + java.util.UUID.randomUUID());
        member.setFullName("Dashboard Test Member");
        member.setPhone("1234567890");
        member.setUsername(username);
        member.setMembershipPlan("Legacy Plan");
        memberRepository.saveAndFlush(member);

        mockMvc.perform(get("/member/dashboard").with(user(username).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome, Dashboard Test Member!")))
                .andExpect(content().string(containsString("Legacy Plan")))
                .andExpect(content().string(containsString("No payments recorded")));
    }

    @Test
    @Transactional
    void membershipPageRendersPlanDatesFromMostRecentPaidMembership() throws Exception {
        String username = "membership-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member();
        member.setMemberId("MEMBERSHIP" + java.util.UUID.randomUUID());
        member.setFullName("Membership Test Member");
        member.setPhone("1234567890");
        member.setUsername(username);
        member.setJoinDate(java.time.LocalDate.now().minusMonths(8));
        member.setMembershipPlan("Old Plan");
        memberRepository.saveAndFlush(member);
        MembershipPlan plan = new MembershipPlan();
        plan.setPlanName("Current Plan " + java.util.UUID.randomUUID().toString().substring(0, 6));
        plan.setPrice(100.0);
        plan.setDurationMonths(3);
        plan.setStatus("Active");
        planRepository.saveAndFlush(plan);
        Payment paid = new Payment();
        paid.setMember(member);
        paid.setMembershipPlan(plan);
        paid.setAmount(100.0);
        paid.setPaymentDate(java.time.LocalDate.now().minusMonths(1));
        paid.setPaymentMethod("Cash");
        paid.setPaymentStatus("Paid");
        paymentRepository.saveAndFlush(paid);

        mockMvc.perform(get("/member/membership").with(user(username).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(plan.getPlanName())))
                .andExpect(content().string(containsString("Active")))
                .andExpect(content().string(containsString("Renew or change your plan")));
    }

    @Test
    @Transactional
    void memberPaymentsPageShowsPendingPaymentAndUnavailableCheckoutClearly() throws Exception {
        String username = "pending-payment-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Member member = new Member();
        member.setMemberId("PENDING" + java.util.UUID.randomUUID());
        member.setFullName("Pending Payment Member");
        member.setPhone("1234567890");
        member.setUsername(username);
        memberRepository.saveAndFlush(member);
        MembershipPlan plan = new MembershipPlan();
        plan.setPlanName("Pending Plan " + java.util.UUID.randomUUID().toString().substring(0, 6));
        plan.setPrice(123.45);
        plan.setDurationMonths(1);
        plan.setStatus("Active");
        planRepository.saveAndFlush(plan);
        Payment pending = new Payment();
        pending.setMember(member);
        pending.setMembershipPlan(plan);
        pending.setAmount(123.45);
        pending.setPaymentDate(java.time.LocalDate.now());
        pending.setPaymentMethod("Registration");
        pending.setPaymentStatus("Pending");
        paymentRepository.saveAndFlush(pending);

        mockMvc.perform(get("/member/payments").with(user(username).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(plan.getPlanName())))
                .andExpect(content().string(containsString("₹123.45")))
                .andExpect(content().string(containsString("Contact the gym to pay")));
    }
}

package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.YookassaCheckoutRequest;
import com.anastasiaeverstova.myeduserver.models.*;
import com.anastasiaeverstova.myeduserver.repository.*;
import com.anastasiaeverstova.myeduserver.yookassa.Yookassa;
import com.anastasiaeverstova.myeduserver.yookassa.model.Amount;
import com.anastasiaeverstova.myeduserver.yookassa.model.Payment;
import com.anastasiaeverstova.myeduserver.yookassa.model.Webhook;
import com.anastasiaeverstova.myeduserver.yookassa.model.WebhookObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
public class YookassaCheckoutController {

    private final UserRepository userRepository;
    private final Yookassa yookassa;

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    public YookassaCheckoutController(UserRepository userRepository, Yookassa yookassa) {
        this.userRepository = userRepository;
        this.yookassa = yookassa;
    }

    @PostMapping(path = "/create")
    public ResponseEntity<String> createPayment(@RequestBody YookassaCheckoutRequest request,
                                                @AuthenticationPrincipal User user) {
        try {
            Integer userId = user.getId();
            String description = "Оплата заказа пользователем ID: " + userId;

            Payment payment = yookassa.createPayment(new Amount(request.getTotalAmount(), "RUB"), description, "http://localhost:5173");

            System.out.println("Created payment with URL: " + payment.getConfirmation().getConfirmationUrl());
            return ResponseEntity.ok(payment.getConfirmation().getConfirmationUrl());
        } catch (Exception e) {
            System.out.println("Error initiating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error initiating payment: " + e.getMessage());
        }
    }

    @PostMapping(path = "/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String rawJson) throws JsonProcessingException {
        System.out.println("Raw JSON received: " + rawJson);
        ObjectMapper objectMapper = new ObjectMapper();
        Webhook webhook = objectMapper.readValue(rawJson, Webhook.class);
        WebhookObject webhookObject = webhook.getObject();

        if (webhookObject != null) {
            System.out.println("Webhook received: " + webhook.getEvent());
            System.out.println("Webhook ID: " + webhookObject.getId());

            if ("payment.succeeded".equals(webhook.getEvent())) {
                try {
                    UUID paymentId = UUID.fromString(webhookObject.getId());
                    Payment payment = yookassa.getPayment(paymentId);
                    Integer userId = extractUserIdFromDescription(payment.description);
                    User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                    Page<Course> coursePage = courseRepository.getCartListByUser(user.getId(), Pageable.unpaged());

                    System.out.println("Course page size: " + coursePage.getTotalElements());
                    List<OrderItem> orderItemList = new ArrayList<>();
                    List<Enrollment> enrollments = new ArrayList<>();

                    Sales savedSale = salesRepository.save(new Sales(paymentId.toString(),
                            user, paymentId.toString(), payment.amount.value));

                    coursePage.forEach(course -> {
                        OrderItem orderItem = new OrderItem(savedSale, course);
                        orderItemList.add(orderItem);
                        System.out.println("Created OrderItem for course: " + course.getId());

                        Enrollment enrollment = new Enrollment(user, course);
                        enrollments.add(enrollment);
                    });

                    System.out.println("Saving " + orderItemList.size() + " order items...");
                    orderItemRepository.saveAll(orderItemList);
                    System.out.println("Order items saved successfully");

                    enrollmentRepository.saveAll(enrollments);
                    List<Integer> courseIds = coursePage.stream().map(Course::getId).collect(Collectors.toList());
                    cartRepository.deleteByUserIdAndCoursesIn(user.getId(), courseIds);
                    wishlistRepository.deleteByUserIdAndCoursesIn(user.getId(), courseIds);

                    return ResponseEntity.ok("Successfully processed payment for USD " + payment.amount.value);
                } catch (Exception e) {
                    System.out.println("Error processing webhook: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid webhook received");
                return ResponseEntity.badRequest().body("Invalid webhook received");
            }
        }
        return ResponseEntity.ok("Webhook processed successfully");
    }


    private Integer extractUserIdFromDescription(String description) {
        Pattern pattern = Pattern.compile("ID: (\\d+)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("User ID not found in payment description");
        }
    }
}

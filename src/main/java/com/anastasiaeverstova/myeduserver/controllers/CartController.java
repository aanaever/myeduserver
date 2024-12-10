package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.models.MyCustomResponse;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.CartRepository;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@RestController
@Secured(value = {"ROLE_STUDENT", "ROLE_ADMIN"})
@RequestMapping(path = "/cart", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping(path = "/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MyCustomResponse> addSingleItem(@AuthenticationPrincipal User user, @PathVariable Integer courseId) {
        try {
            if (user == null || user.getId() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
            Course course = courseRepository.findById(courseId).orElseThrow();
            int count = cartRepository.addToCartCustom(course.getId(), user.getId(), course.getPrice());
            return ResponseEntity.ok(new MyCustomResponse(String.format("Added %d item to Cart", count)));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not add to cart", e);
        }
    }

    @GetMapping(path = "/status/c/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Boolean> checkUserCartItem(@PathVariable @NotNull Integer courseId, @AuthenticationPrincipal User user) {
        boolean inCart = cartRepository.checkIfCourseInCart(user.getId(), courseId) > 0;
        return Collections.singletonMap("inCart", inCart);
    }

    @GetMapping(path = "/mine")
    @ResponseStatus(HttpStatus.OK)
    public Page<Course> getAllMyCartItems(@RequestParam(defaultValue = "0") Integer page, @AuthenticationPrincipal User user) {
        return courseRepository.getCartListByUser(user.getId(), PageRequest.of(Math.abs(page), 5));
    }

    @GetMapping(path = "/mine/bill")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, BigDecimal> getMyCartBill(@AuthenticationPrincipal User user) {
        BigDecimal totalPrice = cartRepository.getTotalBillForUser(user.getId());
        return Collections.singletonMap("totalPrice", totalPrice);
    }

    @GetMapping(path = "/mine/count")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Long> countMyCartItems(@AuthenticationPrincipal User user) {
        long cartCount = cartRepository.countCartByUserIdEquals(user.getId());
        return Collections.singletonMap("cartCount", cartCount);
    }

    @DeleteMapping(path = "/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MyCustomResponse> removeCartByCourseId(@PathVariable @NotNull Integer courseId, @AuthenticationPrincipal User user) {
        int deletedCount = cartRepository.deleteByUserIdAndCoursesIn(user.getId(), Collections.singleton(courseId));
        if (deletedCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not remove from cart");
        }
        return ResponseEntity.ok(new MyCustomResponse("Removed from Cart, course " + courseId));
    }
}

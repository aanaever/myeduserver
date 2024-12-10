package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.ReviewDTO;
import com.anastasiaeverstova.myeduserver.dto.ReviewRequest;
import com.anastasiaeverstova.myeduserver.models.MyCustomResponse;
import com.anastasiaeverstova.myeduserver.models.Review;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.ReviewRepository;
import com.anastasiaeverstova.myeduserver.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @PostMapping(path = "/")
    @Secured(value = "ROLE_STUDENT")
    public ResponseEntity<MyCustomResponse> addCourseReview(@Valid @RequestBody ReviewRequest review,
                                                            @AuthenticationPrincipal User user) {
        try {
            reviewService.addCourseRating(review, user.getId());
            return ResponseEntity.ok().body(new MyCustomResponse("Thanks for your review!"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not add review", e);
        }
    }

    @PutMapping(path = "/id/{id}")
    @Secured(value = "ROLE_STUDENT")
    public ResponseEntity<MyCustomResponse> editCourseReview(@PathVariable Integer id,
                                                             @Valid @RequestBody ReviewRequest review) {
        try {
            reviewService.updateCourseRating(id, review);
            return ResponseEntity.ok().body(new MyCustomResponse("Thanks for your review!"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not edit review", e);
        }
    }

    @GetMapping(path = "/mine/c/{courseId}")
    @Secured(value = "ROLE_STUDENT")
    public ResponseEntity<Review> getMyReviewOnCourse(@PathVariable Integer courseId,
                                                      @AuthenticationPrincipal User user) {
        System.out.println("Getting review for user: " + user.getId() + " and course: " + courseId);
        var reviewOptional = reviewRepository.findByUserIdAndCourseId(user.getId(), courseId);
        if (reviewOptional.isEmpty()) {
            System.out.println("Review not found for user: " + user.getId() + " and course: " + courseId);
        }
        return ResponseEntity.of(reviewOptional);
    }

    @GetMapping(path = "/course/{courseId}")
    public Slice<ReviewDTO> getCourseReviews(@RequestParam(defaultValue = "0") Integer page,
                                             @RequestParam(defaultValue = "createdAt") String sortBy,
                                             @PathVariable Integer courseId) {
        if (Stream.of("createdAt", "rating").noneMatch(sortBy::equals)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid 'sort' param");
        }
        Pageable pageable = PageRequest.of(page, 10, Sort.Direction.DESC, sortBy);
        return reviewRepository.findByCourseId(courseId, pageable);
    }
}
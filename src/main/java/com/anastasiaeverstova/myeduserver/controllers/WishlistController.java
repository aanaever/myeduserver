package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.models.MyCustomResponse;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import com.anastasiaeverstova.myeduserver.repository.WishlistRepository;
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
import java.util.Collections;
import java.util.Map;


@RestController
@Secured("ROLE_STUDENT")
@RequestMapping(path = "/wishlist", produces = MediaType.APPLICATION_JSON_VALUE)
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping(path = "/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MyCustomResponse> addNewWishlist(@PathVariable Integer courseId,
                                                           @AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        int count = wishlistRepository.saveByCourseIdAndUserId(courseId, userId);
        return ResponseEntity.ok(new MyCustomResponse(String.format("Added %d item to Wishlist", count)));
    }

    @GetMapping(path = "/status/c/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Boolean> checkUserLikedCourse(@PathVariable @NotNull Integer courseId,
                                                     @AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        boolean inWishlist = wishlistRepository.checkIfExistWishlistNative(userId, courseId) > 0;
        return Collections.singletonMap("inWishlist", inWishlist);
    }

    @GetMapping(path = "/mine")
    @ResponseStatus(HttpStatus.OK)
    public Page<Course> getMyWishlistPaged(@RequestParam(defaultValue = "0") Integer page,
                                           @AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        return courseRepository.getWishlistByUser(userId, PageRequest.of(Math.abs(page), 5));
    }

    @DeleteMapping(path = "/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MyCustomResponse> removeWishlistByCourseId(@PathVariable @NotNull Integer courseId,
                                                                     @AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        int deletedCount = wishlistRepository.deleteByUserIdAndCoursesIn(userId, Collections.singletonList(courseId));
        if (deletedCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not remove from wishlist");
        }
        return ResponseEntity.ok(new MyCustomResponse("Removed from Wishlist, course " + courseId));
    }
}

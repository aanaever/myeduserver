package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.dto.ReviewRequest;
import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.models.Review;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import com.anastasiaeverstova.myeduserver.repository.ReviewRepository;
import com.anastasiaeverstova.myeduserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public void addCourseRating(ReviewRequest request, Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow();
        Review myReview = new Review(request.getRating(), request.getContent(), user, course);
        reviewRepository.save(myReview);

        double avgRating = reviewRepository.getAverageByCourseId(course.getId());
        course.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.DOWN));
        courseRepository.save(course);
    }

    @Transactional
    public void updateCourseRating(Integer reviewId, ReviewRequest request) {
        Review myReview = reviewRepository.findById(reviewId).orElseThrow();
        Course course = courseRepository.findById(request.getCourseId()).orElseThrow();
        myReview.setRating(request.getRating());
        myReview.setContent(request.getContent());
        reviewRepository.save(myReview);

        double avgRating = reviewRepository.getAverageByCourseId(course.getId());
        course.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.DOWN));
        courseRepository.save(course);
    }
}

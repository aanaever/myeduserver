package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.EnrollmentDTO;
import com.anastasiaeverstova.myeduserver.dto.VideoRequest;
import com.anastasiaeverstova.myeduserver.dto.VideoResponse;
import com.anastasiaeverstova.myeduserver.dto.WatchStatus;
import com.anastasiaeverstova.myeduserver.models.Enrollment;
import com.anastasiaeverstova.myeduserver.models.Lesson;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.EnrollmentRepository;
import com.anastasiaeverstova.myeduserver.repository.LessonRepository;
import com.anastasiaeverstova.myeduserver.service.EnrollProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;


@RestController
@RequestMapping(path = "/enroll", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured(value = {"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER"})
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollProgressService progressService;

    @GetMapping(path = "/status/c/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Boolean> checkEnrollStatus(@PathVariable @NotNull Integer courseId, @AuthenticationPrincipal User user) {
        Map<String, Boolean> response = new HashMap<>(1);
        Integer userId = user.getId();
        boolean isOwned = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        response.put("isOwned", isOwned);
        return response;
    }

    @GetMapping(path = "/progress/summary")
    public List<EnrollmentDTO> getCourseProgress(@AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        return enrollmentRepository.findByUserId(userId, Pageable.ofSize(3));
    }

    @GetMapping(path = "/mine")
    public List<EnrollmentDTO> getAllCourseProgress(@AuthenticationPrincipal User user,
                                                    @RequestParam(defaultValue = "0") Integer page) {
        Integer userId = user.getId();
        return enrollmentRepository.findByUserId(userId, PageRequest.of(page, 10));
    }

    @PostMapping(path = "/videolink/builder")
    public ResponseEntity<VideoResponse> getLessonVideoLink(@AuthenticationPrincipal User user,
                                                            @RequestBody @Valid VideoRequest request) {
        try {
            Integer userId = user.getId();
            Optional<Enrollment> enrollment = enrollmentRepository.getByUserIdAndCourseId(userId, request.getCourseId());
            if (enrollment.isEmpty()) {
                throw new Exception("You don't own this course");
            }
            UUID lessonId = UUID.fromString(request.getLessonId());
            Lesson currentLesson = lessonRepository.findById(lessonId).orElseThrow();
            VideoResponse response = new VideoResponse(enrollment.get().getId(), currentLesson);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Failed! Reason: " + e.getMessage(), e);
        }
    }

    @GetMapping(path = "/resume/c/{courseId}")
    public Map<String, String> resumeMyCourse(@PathVariable Integer courseId, @AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        var enrollment = enrollmentRepository.getByUserIdAndCourseId(userId, courseId);
        if (enrollment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own this course");
        }
        Optional<Lesson> nextLesson = progressService.getNextLesson(enrollment.get());
        if (nextLesson.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not get lesson!");
        }
        return Collections.singletonMap("lessonId", String.valueOf(nextLesson.get().getId()));
    }

    @PostMapping(path = "/watched")
    @CacheEvict(value = "student-summary", key = "#user.id")
    public Map<String, String> updateWatchStatus(@RequestBody @Valid WatchStatus status, @AuthenticationPrincipal User user) {
        try {
            Integer userId = user.getId();  // Извлечение userId
            Optional<Enrollment> enrollment = enrollmentRepository.getByUserIdAndCourseId(userId, status.getCourseId());
            if (enrollment.isEmpty()) throw new Exception("You don't own this course");

            Map<String, String> response = new HashMap<>(2);
            Optional<Lesson> nextLesson = progressService.updateAndGetNextLesson(status, enrollment.get());
            if (nextLesson.isPresent()) {
                response.put("nextLessonId", String.valueOf(nextLesson.get().getId()));
            } else {
                response.put("nextLessonId", null);
                response.put("message", "Congrats! You completed the course!");
            }
            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not update status: " + e.getMessage(), e);
        }
    }
}

package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.models.Lesson;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.EnrollmentRepository;
import com.anastasiaeverstova.myeduserver.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/lessons", produces = MediaType.APPLICATION_JSON_VALUE)
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @GetMapping(path = "/course/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Secured({"ROLE_STUDENT", "ROLE_ADMIN"})
    public Slice<Lesson> getLessonsByCourseId(@PathVariable @NotNull Integer id,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @AuthenticationPrincipal User user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return lessonRepository.getLessonsByCourseId(id, PageRequest.of(page, 10));
        } else {
            Integer userId = user.getId();
            if (!enrollmentRepository.existsByUserIdAndCourseId(userId, id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own this course");
            }
            return lessonRepository.getLessonsByCourseId(id, PageRequest.of(page, 10));
        }
    }

    @GetMapping(path = "/c/{courseId}/e/{enrollId}")
    @ResponseStatus(HttpStatus.OK)
    @Secured({"ROLE_STUDENT", "ROLE_ADMIN"})
    public List<Map<String, Object>> getAllMyLessonsInEnrollment(@PathVariable Integer courseId,
                                                                 @PathVariable Long enrollId,
                                                                 @AuthenticationPrincipal User user) {
        Integer userId = user.getId();
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't own this course");
        }
        return lessonRepository.getWatchStatusListByEnrollment(enrollId, courseId);
    }

    @PostMapping("/uploadVideo/{lessonId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> uploadVideo(@PathVariable UUID lessonId, @RequestParam("file") MultipartFile file) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        try {
            String filename = file.getOriginalFilename();
            Path targetLocation = Paths.get("video_storage_directory", filename);
            file.transferTo(targetLocation);

            lesson.setVideokey(targetLocation.toString());
            lessonRepository.save(lesson);
            return ResponseEntity.ok("Video uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload video");
        }
    }
}

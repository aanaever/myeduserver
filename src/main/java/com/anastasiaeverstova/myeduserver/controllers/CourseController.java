package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.CategoryDTO;
import com.anastasiaeverstova.myeduserver.dto.CourseDTO;
import com.anastasiaeverstova.myeduserver.dto.FullCourseDTO;
import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import com.anastasiaeverstova.myeduserver.service.CourseService;
import com.anastasiaeverstova.myeduserver.service.FullCourseService;
import com.anastasiaeverstova.myeduserver.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/courses", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private FullCourseService fullCourseService;
    @Autowired
    private final VideoService videoService;
    @Autowired
    private CourseService courseService;

    public CourseController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping(path = "/id/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable @NotNull Integer id) {
        return ResponseEntity.of(courseRepository.findById(id));
    }

    @GetMapping(path = "/cat/{category}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<Course> getCoursesByCategory(@PathVariable @NotBlank String category) {
        var courseList = courseRepository.getCoursesByCategoryEquals(category);
        if (courseList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No results for given category");
        }
        return courseList;
    }

    @GetMapping(path = "/top")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<List<Course>> getAllTopCourses() {
        var courseList = courseRepository.getTop6CoursesByIsFeatured(true);
        CacheControl cc = CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic();
        return ResponseEntity.ok().cacheControl(cc).body(courseList);
    }

    @GetMapping(path = "/categories")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<List<CategoryDTO>> getCategoryListDistinct() {
        var categoryDTO = courseRepository.getAllDistinctCategories();
        CacheControl cc = CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic();
        return ResponseEntity.ok().cacheControl(cc).body(categoryDTO);
    }

    @GetMapping(path = "/search")
    @ResponseStatus(value = HttpStatus.OK)
    public Slice<Course> searchForCourseByTitle(@RequestParam(defaultValue = "") @NotBlank String title,
                                                @RequestParam(defaultValue = "0") Integer page) {
        if (title.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query too short");
        }
        return courseRepository.getCoursesByTitleContaining(title, PageRequest.of(page, 10));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = (List<Course>) courseRepository.findAll();
        return ResponseEntity.ok(courses);
    }

    @DeleteMapping(path = "/id/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCourseById(@PathVariable Integer id) {
        if (!courseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        courseRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Integer courseId, @RequestBody CourseDTO courseDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with ID: " + courseId));

        course.setTitle(courseDTO.getTitle());
        course.setSubtitle(courseDTO.getSubtitle());
        course.setPrice(courseDTO.getPrice());
        course.setCategory(courseDTO.getCategory());
        course.setAuthor(courseDTO.getAuthor());
        course.setStatus(courseDTO.getStatus());

        Course freshCourse = courseRepository.save(course);
        CourseDTO responseDTO = new CourseDTO(freshCourse.getId(), freshCourse.getTitle(), freshCourse.getSubtitle(),
                freshCourse.getAuthor(), freshCourse.getCategory(), freshCourse.getRating(), freshCourse.getThumbUrl(),
                freshCourse.getPrice(), freshCourse.getStatus());

        return ResponseEntity.ok().body(responseDTO);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDto) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setSubtitle(courseDto.getSubtitle());
        course.setAuthor(courseDto.getAuthor());
        course.setPrice(courseDto.getPrice());
        course.setCategory(courseDto.getCategory());
        course.setThumbUrl(courseDto.getThumbUrl());
        course.setStatus(courseDto.getStatus());

        Course createdCourse = courseRepository.save(course);

        CourseDTO responseDTO = new CourseDTO(createdCourse.getId(), createdCourse.getTitle(), createdCourse.getSubtitle(),
                createdCourse.getAuthor(), createdCourse.getCategory(), createdCourse.getRating(), createdCourse.getThumbUrl(),
                createdCourse.getPrice(), createdCourse.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/full")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> createFullCourse(@RequestBody FullCourseDTO fullCourseDTO) {
        try {
            Course createdCourse = fullCourseService.createFullCourse(fullCourseDTO);
            CourseDTO responseDTO = new CourseDTO(createdCourse.getId(), createdCourse.getTitle(), createdCourse.getSubtitle(),
                    createdCourse.getAuthor(), createdCourse.getCategory(), createdCourse.getRating(), createdCourse.getThumbUrl(),
                    createdCourse.getPrice(), createdCourse.getStatus());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            System.err.println("Ошибка при создании курса: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/uploadVideo")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<String> uploadVideo(@RequestParam("video") MultipartFile video) {
        System.out.println("Received video file: " + video.getOriginalFilename());
        try {
            String filename = videoService.saveVideo(video);
            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка при сохранении видео: " + e.getMessage());
        }
    }

    @GetMapping("/video/{filename}")
    //@PreAuthorize("hasAuthority('ROLE_STUDENT')")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<Resource> getVideo(@PathVariable String filename) {
        try {
            Resource video = videoService.loadVideo(filename);
            return ResponseEntity.ok()
                    .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(video);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/full/{courseId}")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<?> updateFullCourse(@PathVariable Integer courseId, @RequestBody FullCourseDTO fullCourseDTO) {
        try {
            Course updatedCourse = fullCourseService.updateFullCourse(courseId, fullCourseDTO);
            CourseDTO responseDTO = new CourseDTO(updatedCourse.getId(), updatedCourse.getTitle(), updatedCourse.getSubtitle(),
                    updatedCourse.getAuthor(), updatedCourse.getCategory(), updatedCourse.getRating(), updatedCourse.getThumbUrl(),
                    updatedCourse.getPrice(), updatedCourse.getStatus());
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении курса: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/full/{courseId}")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<FullCourseDTO> getFullCourse(@PathVariable Integer courseId) {
        try {
            FullCourseDTO fullCourseDTO = fullCourseService.getFullCourse(courseId);
            return ResponseEntity.ok(fullCourseDTO);
        } catch (Exception e) {
            System.err.println("Ошибка при получении полного курса: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<Course>> getCoursesByAuthor(@PathVariable String author) {
        List<Course> courses = courseRepository.findByAuthor(author);
        if (courses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(courses);
    }
}

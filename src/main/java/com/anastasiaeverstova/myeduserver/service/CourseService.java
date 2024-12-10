package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.dto.CourseDTO;
import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.models.CourseStatus;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public void updateCourseStatus(Integer id, CourseStatus status) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        course.setStatus(status);
        courseRepository.save(course);
    }

    public List<CourseDTO> getCoursesByStatus(CourseStatus status) {
        List<Course> courses = courseRepository.findByStatus(status);
        return courses.stream().map(course -> new CourseDTO(course.getId(), course.getTitle(),
                course.getSubtitle(), course.getAuthor(), course.getCategory(), course.getRating(),
                course.getThumbUrl(), course.getPrice(), course.getStatus())).collect(Collectors.toList());
    }
}

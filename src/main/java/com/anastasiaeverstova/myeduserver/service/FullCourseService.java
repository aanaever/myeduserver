package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.dto.CourseDTO;
import com.anastasiaeverstova.myeduserver.dto.FullCourseDTO;
import com.anastasiaeverstova.myeduserver.dto.LessonDTO;
import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.models.CourseObjective;
import com.anastasiaeverstova.myeduserver.models.Lesson;
import com.anastasiaeverstova.myeduserver.models.CourseStatus;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import com.anastasiaeverstova.myeduserver.repository.LessonRepository;
import com.anastasiaeverstova.myeduserver.repository.ObjectiveRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FullCourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Autowired
    private LessonRepository lessonRepository;

    public Course createFullCourse(FullCourseDTO fullCourseDTO) {
        if (fullCourseDTO == null || fullCourseDTO.getCourse() == null) {
            throw new IllegalArgumentException("FullCourseDTO or CourseDTO is null");
        }

        Course course = new Course();
        course.setTitle(fullCourseDTO.getCourse().getTitle());
        course.setSubtitle(fullCourseDTO.getCourse().getSubtitle());
        course.setAuthor(fullCourseDTO.getCourse().getAuthor());
        course.setPrice(fullCourseDTO.getCourse().getPrice());
        course.setCategory(fullCourseDTO.getCourse().getCategory());
        course.setThumbUrl(fullCourseDTO.getCourse().getThumbUrl());
        course.setRating(fullCourseDTO.getCourse().getRating() != null ? fullCourseDTO.getCourse().getRating() : BigDecimal.ZERO);
        course.setIsFeatured(false);
        course.setStatus(fullCourseDTO.getCourse().getStatus() != null ? fullCourseDTO.getCourse().getStatus() : CourseStatus.PENDING_REVIEW);

        final Course savedCourse = courseRepository.save(course);

        fullCourseDTO.getObjectives().forEach(obj -> {
            CourseObjective objective = new CourseObjective();
            objective.setCourse(savedCourse);
            objective.setObjective(obj);
            objectiveRepository.save(objective);
        });

        fullCourseDTO.getLessons().forEach(lessonDTO -> {
            Lesson lesson = new Lesson();
            lesson.setCourse(savedCourse);
            lesson.setLessonName(lessonDTO.getLessonName());
            lesson.setVideokey(lessonDTO.getVideokey());
            lesson.setLengthSeconds(lessonDTO.getLengthSeconds());
            lesson.setPosition(lessonDTO.getPosition());
            lessonRepository.save(lesson);
        });

        return savedCourse;
    }
    public Course updateFullCourse(Integer courseId, FullCourseDTO fullCourseDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with ID: " + courseId));

        course.setTitle(fullCourseDTO.getCourse().getTitle());
        course.setSubtitle(fullCourseDTO.getCourse().getSubtitle());
        course.setAuthor(fullCourseDTO.getCourse().getAuthor());
        course.setPrice(fullCourseDTO.getCourse().getPrice());
        course.setCategory(fullCourseDTO.getCourse().getCategory());
        course.setThumbUrl(fullCourseDTO.getCourse().getThumbUrl());
        course.setStatus(fullCourseDTO.getCourse().getStatus());

        objectiveRepository.deleteByCourseId(courseId);
        fullCourseDTO.getObjectives().forEach(obj -> {
            CourseObjective objective = new CourseObjective();
            objective.setCourse(course);
            objective.setObjective(obj);
            objectiveRepository.save(objective);
        });


        lessonRepository.deleteByCourseId(courseId);
        fullCourseDTO.getLessons().forEach(lessonDTO -> {
            Lesson lesson = new Lesson();
            lesson.setCourse(course);
            lesson.setLessonName(lessonDTO.getLessonName());
            lesson.setVideokey(lessonDTO.getVideokey());
            lesson.setLengthSeconds(lessonDTO.getLengthSeconds());
            lesson.setPosition(lessonDTO.getPosition());
            lessonRepository.save(lesson);
        });

        return courseRepository.save(course);
    }

    public FullCourseDTO getFullCourse(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with ID: " + courseId));

        List<String> objectives = objectiveRepository.getCourseObjectivesByCourseId(courseId)
                .stream()
                .map(CourseObjective::getObjective)
                .collect(Collectors.toList());

        List<LessonDTO> lessons = lessonRepository.getLessonsByCourseId(courseId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(lesson -> {
                    LessonDTO lessonDTO = new LessonDTO();
                    lessonDTO.setId(lesson.getId());
                    lessonDTO.setLessonName(lesson.getLessonName());
                    lessonDTO.setVideokey(lesson.getVideokey());
                    lessonDTO.setLengthSeconds(lesson.getLengthSeconds());
                    lessonDTO.setPosition(lesson.getPosition());
                    return lessonDTO;
                })
                .collect(Collectors.toList());

        CourseDTO courseDTO = new CourseDTO(course.getId(), course.getTitle(), course.getSubtitle(),
                course.getAuthor(), course.getCategory(), course.getRating(), course.getThumbUrl(),
                course.getPrice(), course.getStatus());

        return new FullCourseDTO(courseDTO, objectives, lessons);
    }

}

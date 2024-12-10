package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FullCourseDTO {
    private CourseDTO course;
    private List<String> objectives;
    private List<LessonDTO> lessons;

    public FullCourseDTO(CourseDTO course, List<String> objectives, List<LessonDTO> lessons) {
        this.course = course;
        this.objectives = objectives;
        this.lessons = lessons;
    }
}
package com.anastasiaeverstova.myeduserver.controllers;

import com.anastasiaeverstova.myeduserver.dto.ObjectivesDTO;
import com.anastasiaeverstova.myeduserver.models.Course;
import com.anastasiaeverstova.myeduserver.models.CourseObjective;
import com.anastasiaeverstova.myeduserver.models.MyCustomResponse;
import com.anastasiaeverstova.myeduserver.repository.CourseRepository;
import com.anastasiaeverstova.myeduserver.repository.ObjectiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/objectives", produces = MediaType.APPLICATION_JSON_VALUE)
public class ObjectivesController {

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping(value = "/")
    @Secured(value = "ROLE_ADMIN")
    public ResponseEntity<MyCustomResponse> addNewObjectives(@RequestBody @Valid ObjectivesDTO objDTO) {
        List<String> objectives = objDTO.getObjectives();
        try {
            Course course = courseRepository.findById(objDTO.getCourseId()).orElseThrow();
            List<CourseObjective> coList = objectives.stream().map(o -> new CourseObjective(course, o)).collect(Collectors.toList());
            objectiveRepository.saveAll(coList);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MyCustomResponse("All saved!"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not save new objectives", e);
        }
    }

    @GetMapping(value = "/course/{courseId}")
    public List<CourseObjective> getCourseObjectives(@PathVariable Integer courseId) {
        return objectiveRepository.getCourseObjectivesByCourseId(courseId);
    }
}
package com.anastasiaeverstova.myeduserver.repository;

import com.anastasiaeverstova.myeduserver.models.CourseObjective;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectiveRepository extends CrudRepository<CourseObjective, Integer> {

    List<CourseObjective> getCourseObjectivesByCourseId(Integer course_id);
    @Modifying
    @Query("DELETE FROM CourseObjective o WHERE o.course.id = ?1")
    void deleteByCourseId(Integer courseId);

}


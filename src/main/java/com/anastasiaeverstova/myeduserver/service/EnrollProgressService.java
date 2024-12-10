package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.dto.WatchStatus;
import com.anastasiaeverstova.myeduserver.models.EnrollProgress;
import com.anastasiaeverstova.myeduserver.models.Enrollment;
import com.anastasiaeverstova.myeduserver.models.Lesson;
import com.anastasiaeverstova.myeduserver.repository.EnrollProgressRepository;
import com.anastasiaeverstova.myeduserver.repository.EnrollmentRepository;
import com.anastasiaeverstova.myeduserver.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnrollProgressService {

    @Autowired
    private EnrollProgressRepository progressRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @Transactional
    public Optional<Lesson> updateAndGetNextLesson(@NotNull WatchStatus status, Enrollment enrollment) {
        UUID lessonId = UUID.fromString(status.getCurrentLessonId());
        Lesson currentLesson = lessonRepository.findById(lessonId).orElseThrow();
        Optional<EnrollProgress> enrollProgress = progressRepository.findByEnrollIdAndLessonId(status.getEnrollId(), lessonId);

        if (enrollProgress.isEmpty()) {
            progressRepository.save(new EnrollProgress(enrollment, currentLesson));

            long numWatched = progressRepository.countByEnrollmentId(enrollment.getId());
            long totalLessons = lessonRepository.countByCourseId(status.getCourseId());
            double percentVal = (double) numWatched / (double) totalLessons * 100.00;
            boolean isCompleted = (percentVal / 100.00) == 1;

            BigDecimal progressPercent = BigDecimal.valueOf(percentVal).setScale(2, RoundingMode.HALF_UP);
            enrollment.setProgress(progressPercent);
            enrollment.setIsCompleted(isCompleted);
            if (!isCompleted) {
                enrollment.setNextPosition(currentLesson.getPosition() + 1);
            } else {
                enrollment.setNextPosition(1);
            }
            enrollmentRepository.save(enrollment);
            return this.getNextLesson(enrollment);
        }

        return this.getNextLesson(enrollment);
    }


    public Optional<Lesson> getNextLesson(@NotNull Enrollment enrollment) {
        if (enrollment == null || enrollment.getCourse() == null) {
            System.out.println("Enrollment or its course is null.");
            return Optional.empty();
        }
        Integer nextPosition = enrollment.getNextPosition();
        Integer courseId = enrollment.getCourse().getId();
        Optional<Lesson> next = lessonRepository.getFirstNotWatchedInEnrollment(enrollment.getId(), courseId);
        return next.or(() -> lessonRepository.findByCourseIdAndPosition(courseId, nextPosition));
    }

}

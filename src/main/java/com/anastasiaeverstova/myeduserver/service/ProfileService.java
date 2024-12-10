package com.anastasiaeverstova.myeduserver.service;

import com.anastasiaeverstova.myeduserver.dto.StudentSummary;
import com.anastasiaeverstova.myeduserver.models.SummaryTitle;
import com.anastasiaeverstova.myeduserver.models.User;
import com.anastasiaeverstova.myeduserver.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public List<StudentSummary> getUserSummaryList(@NotNull User user) {
        List<StudentSummary> summaryList = new ArrayList<>();
        long owned = enrollmentRepository.countEnrollmentByUser(user);
        StudentSummary s1 = new StudentSummary(SummaryTitle.OWNING, owned, "courses");
        summaryList.add(s1);

        long completed = enrollmentRepository.countEnrollmentByUserAndIsCompleted(user, true);
        StudentSummary s2 = new StudentSummary(SummaryTitle.COMPLETED, completed, "courses");
        summaryList.add(s2);

        Duration duration = Duration.between(Instant.now(), user.getCreatedAt()).abs();
        final long numberDays = duration.toDays();

        long result = 0;
        String units;
        if (numberDays == 0) units = "today";
        else if (numberDays > 0 && numberDays <= 30) {
            result = numberDays;
            units = "days ago";
        } else if (numberDays > 30 && numberDays <= 365) {
            result = Math.floorDiv(duration.toDays(), 30);
            units = "month(s) ago";
        } else {
            result = Math.floorDiv(duration.toDays(), 365);
            units = "year(s) ago";
        }

        StudentSummary s3 = new StudentSummary(SummaryTitle.JOINED, result, units);
        summaryList.add(s3);
        return summaryList;
    }
}

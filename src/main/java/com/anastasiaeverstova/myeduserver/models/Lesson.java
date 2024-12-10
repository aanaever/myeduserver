package com.anastasiaeverstova.myeduserver.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "lessons", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "position"}))
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @NotBlank
    private String lessonName;

    @NotBlank
    @Column(nullable = false)
    private String videokey;

    @NotNull
    @ColumnDefault("0")
    private Integer lengthSeconds;

    @NotNull
    @Column(nullable = false)
    private Integer position;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    private Course course;

    public boolean isUrl() {
        return videokey != null && (videokey.startsWith("http://") || videokey.startsWith("https://"));
    }

    public String getVideoPath() {
        return isUrl() ? null : videokey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Lesson lesson = (Lesson) o;
        return id != null && Objects.equals(id, lesson.id);
    }


    public String getLengthSecondsFormatted() {
        return String.format("%02d:%02d", Duration.ofSeconds(this.lengthSeconds).toMinutesPart(),
                Duration.ofSeconds(this.lengthSeconds).toSecondsPart());
    }

    public Integer getLengthSeconds() {
        return this.lengthSeconds;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

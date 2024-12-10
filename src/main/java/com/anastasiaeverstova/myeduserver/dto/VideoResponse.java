package com.anastasiaeverstova.myeduserver.dto;

import com.anastasiaeverstova.myeduserver.models.Lesson;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class VideoResponse {
    @NotNull
    private final Long enrollId;
    @NotNull
    private final Lesson lesson;

    public VideoResponse(Long enrollId, Lesson lesson) {
        this.enrollId = enrollId;
        this.lesson = lesson;
    }
}

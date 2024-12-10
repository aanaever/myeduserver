package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class WatchStatus {
    @NotNull
    private Long enrollId;
    @NotNull
    private String currentLessonId;
    @NotNull
    private Integer courseId;

    public WatchStatus() {
    }
}

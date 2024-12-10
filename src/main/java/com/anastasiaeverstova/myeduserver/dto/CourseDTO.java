package com.anastasiaeverstova.myeduserver.dto;

import com.anastasiaeverstova.myeduserver.models.CourseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CourseDTO {

    private Integer id;
    private String title;
    private String subtitle;
    private String author;
    private String category;
    private BigDecimal rating;
    private String thumbUrl;
    private BigDecimal price;
    private Boolean isFeatured;
    private CourseStatus status;

    public CourseDTO(Integer id, String title, String subtitle, String author, String category, BigDecimal rating, String thumbUrl, BigDecimal price, CourseStatus status) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.author = author;
        this.category = category;
        this.rating = rating;
        this.thumbUrl = thumbUrl;
        this.price = price;
        this.isFeatured = false;
        this.status = status;
    }
}


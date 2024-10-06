package dev.ray.adbatch.domain.advertisement.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Advertisement {

    private Long id;
    private Integer shopId;
    private String name;
    private AdvertisementCategory category;
    private AdvertisementStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}

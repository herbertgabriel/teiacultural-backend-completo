package com.proa.teiacultural.controller.dto;

public record CreatePublicationDto(
        String content,
        String imageUrl1,
        String imageUrl2,
        String imageUrl3,
        String imageUrl4
) {}
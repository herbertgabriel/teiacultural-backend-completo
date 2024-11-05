package com.proa.teiacultural.controller.dto.PublicationDto;

import org.springframework.web.multipart.MultipartFile;

public record UpdatePublicationDto(
        String content,
        MultipartFile imageUrl1,
        MultipartFile imageUrl2,
        MultipartFile imageUrl3,
        MultipartFile imageUrl4) {
}
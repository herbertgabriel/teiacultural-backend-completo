package com.proa.teiacultural.controller.dto.UserDto;

public record CreatePremiumDetailsDto(
    String professionalName,
    String category,
    String aboutMe,
    String socialMedia,
    String localization,
    String profilePicture
) {
}
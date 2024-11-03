package com.proa.teiacultural.controller.dto.UserDto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id,
    String username,
    String category,
    String professionalName
) {
}
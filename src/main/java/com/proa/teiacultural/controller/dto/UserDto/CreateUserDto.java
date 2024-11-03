package com.proa.teiacultural.controller.dto.UserDto;

public record CreateUserDto(
        String email,
        String password,
        String name,
        String cpf,
        String telephone
) {
}
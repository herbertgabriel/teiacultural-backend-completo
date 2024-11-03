package com.proa.teiacultural.controller.dto;

public record CreateUserDto(
        String email,
        String password,
        String name,
        String cpf,
        String telephone
) {
}
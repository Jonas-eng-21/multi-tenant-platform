package br.com.jonas.multitenant.pessoa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PessoaUpdateRequest(
        @NotBlank String nome,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos") String cpf,
        @NotNull LocalDate dataNascimento,
        @Email String email
) {
}

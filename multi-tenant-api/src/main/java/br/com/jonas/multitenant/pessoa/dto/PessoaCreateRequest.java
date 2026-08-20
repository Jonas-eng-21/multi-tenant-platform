package br.com.jonas.multitenant.pessoa.dto;

import br.com.jonas.multitenant.common.validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaCreateRequest(
        @NotBlank String nome,
        @NotBlank @CPF String cpf,
        @NotNull LocalDate dataNascimento,
        @Email String email
) {
}

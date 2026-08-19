package br.com.jonas.multitenant.pessoa.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PessoaResponse(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

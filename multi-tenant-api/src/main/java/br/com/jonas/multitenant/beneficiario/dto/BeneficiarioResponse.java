package br.com.jonas.multitenant.beneficiario.dto;

import br.com.jonas.multitenant.beneficiario.entity.StatusBeneficiario;
import br.com.jonas.multitenant.beneficiario.entity.TipoBeneficiario;
import br.com.jonas.multitenant.pessoa.dto.PessoaResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BeneficiarioResponse(
        UUID id,
        PessoaResponse pessoa,
        UUID tenantId,
        String matricula,
        TipoBeneficiario tipo,
        StatusBeneficiario status,
        LocalDate dataAdesao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

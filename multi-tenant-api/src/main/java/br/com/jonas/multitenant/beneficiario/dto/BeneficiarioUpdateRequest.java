package br.com.jonas.multitenant.beneficiario.dto;

import br.com.jonas.multitenant.beneficiario.entity.StatusBeneficiario;
import br.com.jonas.multitenant.beneficiario.entity.TipoBeneficiario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BeneficiarioUpdateRequest(
        @NotBlank @Size(max = 50) String matricula,
        @NotNull TipoBeneficiario tipo,
        @NotNull StatusBeneficiario status,
        @NotNull LocalDate dataAdesao
) {
}

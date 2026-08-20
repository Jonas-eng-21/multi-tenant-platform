package br.com.jonas.multitenant.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estrutura padronizada de erros da API")
public record ApiErrorResponse(
        @Schema(description = "Data e hora do erro", example = "2026-08-20T07:45:00Z") Instant timestamp,
        @Schema(description = "Código HTTP do erro", example = "400") int status,
        @Schema(description = "Descrição curta do erro", example = "Bad Request") String error,
        @Schema(description = "Mensagem detalhada do erro", example = "Erro de validação nos campos informados") String message,
        @Schema(description = "Caminho da requisição que gerou o erro", example = "/api/pessoas") String path,
        @Schema(description = "Mapa de erros de validação por campo, se aplicável", example = "{\"cpf\": \"CPF inválido\"}") Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse withFieldErrors(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}

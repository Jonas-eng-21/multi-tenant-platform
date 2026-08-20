package br.com.jonas.multitenant.common.dto;

import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta paginada genérica contendo os dados e metadados de paginação")
public record PageResponse<T>(
        @Schema(description = "Lista de itens retornados na página atual") List<T> content,
        @Schema(description = "Número da página atual (iniciando em 0)", example = "0") int page,
        @Schema(description = "Tamanho de itens solicitados por página", example = "10") int size,
        @Schema(description = "Total absoluto de elementos disponíveis", example = "25") long totalElements,
        @Schema(description = "Total de páginas disponíveis", example = "3") int totalPages,
        @Schema(description = "Indica se esta é a primeira página", example = "true") boolean first,
        @Schema(description = "Indica se esta é a última página", example = "false") boolean last
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}

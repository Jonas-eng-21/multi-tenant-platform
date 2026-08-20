package br.com.jonas.multitenant.pessoa.controller;

import br.com.jonas.multitenant.pessoa.dto.PessoaCreateRequest;
import br.com.jonas.multitenant.pessoa.dto.PessoaResponse;
import br.com.jonas.multitenant.pessoa.dto.PessoaUpdateRequest;
import br.com.jonas.multitenant.pessoa.service.PessoaService;
import jakarta.validation.Valid;
import br.com.jonas.multitenant.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import br.com.jonas.multitenant.exception.ApiErrorResponse;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/pessoas")
@Tag(name = "Pessoas", description = "Endpoints para gerenciamento de pessoas")
@SecurityRequirement(name = "bearerAuth")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @PostMapping
    @Operation(summary = "Criar nova pessoa", description = "Cria uma nova pessoa com os dados fornecidos, validando o CPF")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (ex: CPF inválido)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PessoaResponse> create(@RequestBody @Valid PessoaCreateRequest request) {
        PessoaResponse response = pessoaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar pessoas", description = "Retorna uma lista paginada de pessoas, permitindo filtros e ordenação")
    public PageResponse<PessoaResponse> findAll(
            @Parameter(description = "Filtro opcional por nome (contém, case-insensitive)") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro opcional por CPF (exato)") @RequestParam(required = false) String cpf,
            @Parameter(description = "Configuração de paginação e ordenação (ex: page=0&size=10&sort=nome,asc)") Pageable pageable
    ) {
        return pessoaService.findAll(nome, cpf, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pessoa por ID", description = "Retorna os detalhes de uma pessoa específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PessoaResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(pessoaService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pessoa", description = "Atualiza os dados de uma pessoa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado em outra pessoa", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PessoaResponse> update(@PathVariable UUID id, @RequestBody @Valid PessoaUpdateRequest request) {
        return ResponseEntity.ok(pessoaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pessoa", description = "Exclui uma pessoa caso não possua vínculos ativos (beneficiários)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pessoa excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Pessoa possui vínculos e não pode ser excluída", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        pessoaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

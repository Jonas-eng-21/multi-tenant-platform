package br.com.jonas.multitenant.beneficiario.controller;

import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioCreateRequest;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioResponse;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioUpdateRequest;
import br.com.jonas.multitenant.beneficiario.service.BeneficiarioService;
import jakarta.validation.Valid;
import br.com.jonas.multitenant.beneficiario.entity.StatusBeneficiario;
import br.com.jonas.multitenant.beneficiario.entity.TipoBeneficiario;
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
@RequestMapping("/api/beneficiarios")
@Tag(name = "Beneficiários", description = "Endpoints para gerenciamento de beneficiários. O tenant é inferido automaticamente pelo token JWT.")
@SecurityRequirement(name = "bearerAuth")
public class BeneficiarioController {

    private final BeneficiarioService beneficiarioService;

    public BeneficiarioController(BeneficiarioService beneficiarioService) {
        this.beneficiarioService = beneficiarioService;
    }

    @PostMapping
    @Operation(summary = "Criar novo beneficiário", description = "Cria um beneficiário vinculado ao tenant do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Beneficiário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Matrícula já cadastrada no mesmo tenant", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<BeneficiarioResponse> create(@RequestBody @Valid BeneficiarioCreateRequest request) {
        BeneficiarioResponse response = beneficiarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar beneficiários", description = "Retorna lista paginada de beneficiários do tenant atual")
    public PageResponse<BeneficiarioResponse> findAll(
            @Parameter(description = "Filtro opcional por matrícula (contém, case-insensitive)") @RequestParam(required = false) String matricula,
            @Parameter(description = "Filtro opcional por status exato (ex: ATIVO, INATIVO)") @RequestParam(required = false) StatusBeneficiario status,
            @Parameter(description = "Filtro opcional por tipo exato (ex: TITULAR, DEPENDENTE)") @RequestParam(required = false) TipoBeneficiario tipo,
            @Parameter(description = "Configuração de paginação (ex: page=0&size=10)") Pageable pageable
    ) {
        return beneficiarioService.findAll(matricula, status, tipo, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar beneficiário por ID", description = "Busca beneficiário pelo ID. Lança 404 se não pertencer ao tenant atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiário encontrado"),
            @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado neste tenant", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<BeneficiarioResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(beneficiarioService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar beneficiário", description = "Atualiza os dados de um beneficiário do tenant atual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Beneficiário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado neste tenant", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Matrícula já cadastrada para outro beneficiário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<BeneficiarioResponse> update(@PathVariable UUID id, @RequestBody @Valid BeneficiarioUpdateRequest request) {
        return ResponseEntity.ok(beneficiarioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir beneficiário", description = "Exclui um beneficiário do tenant atual")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Beneficiário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado neste tenant", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        beneficiarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

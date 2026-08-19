package br.com.jonas.multitenant.beneficiario.controller;

import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioCreateRequest;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioResponse;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioUpdateRequest;
import br.com.jonas.multitenant.beneficiario.service.BeneficiarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/beneficiarios")
public class BeneficiarioController {

    private final BeneficiarioService beneficiarioService;

    public BeneficiarioController(BeneficiarioService beneficiarioService) {
        this.beneficiarioService = beneficiarioService;
    }

    @PostMapping
    public ResponseEntity<BeneficiarioResponse> create(@Valid @RequestBody BeneficiarioCreateRequest request) {
        BeneficiarioResponse response = beneficiarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public Page<BeneficiarioResponse> findAll(
            @RequestParam(required = false) String matricula,
            Pageable pageable
    ) {
        return beneficiarioService.findAll(matricula, pageable);
    }

    @GetMapping("/{id}")
    public BeneficiarioResponse findById(@PathVariable UUID id) {
        return beneficiarioService.findById(id);
    }

    @PutMapping("/{id}")
    public BeneficiarioResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody BeneficiarioUpdateRequest request
    ) {
        return beneficiarioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        beneficiarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

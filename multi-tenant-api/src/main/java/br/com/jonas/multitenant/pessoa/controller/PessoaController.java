package br.com.jonas.multitenant.pessoa.controller;

import br.com.jonas.multitenant.pessoa.dto.PessoaCreateRequest;
import br.com.jonas.multitenant.pessoa.dto.PessoaResponse;
import br.com.jonas.multitenant.pessoa.dto.PessoaUpdateRequest;
import br.com.jonas.multitenant.pessoa.service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @PostMapping
    public ResponseEntity<PessoaResponse> create(@Valid @RequestBody PessoaCreateRequest request) {
        PessoaResponse response = pessoaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public Page<PessoaResponse> findAll(
            @RequestParam(required = false) String nome,
            Pageable pageable
    ) {
        return pessoaService.findAll(nome, pageable);
    }

    @GetMapping("/{id}")
    public PessoaResponse findById(@PathVariable UUID id) {
        return pessoaService.findById(id);
    }

    @PutMapping("/{id}")
    public PessoaResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody PessoaUpdateRequest request
    ) {
        return pessoaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        pessoaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

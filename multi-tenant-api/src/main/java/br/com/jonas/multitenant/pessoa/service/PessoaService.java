package br.com.jonas.multitenant.pessoa.service;

import br.com.jonas.multitenant.exception.ConflictException;
import br.com.jonas.multitenant.exception.ResourceNotFoundException;
import br.com.jonas.multitenant.pessoa.dto.PessoaCreateRequest;
import br.com.jonas.multitenant.pessoa.dto.PessoaResponse;
import br.com.jonas.multitenant.pessoa.dto.PessoaUpdateRequest;
import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import br.com.jonas.multitenant.pessoa.repository.PessoaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import br.com.jonas.multitenant.common.dto.PageResponse;
import br.com.jonas.multitenant.pessoa.repository.PessoaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public PessoaResponse create(PessoaCreateRequest request) {
        pessoaRepository.findByCpf(request.cpf())
                .ifPresent(p -> {
                    throw new ConflictException("CPF já cadastrado");
                });

        Pessoa pessoa = new Pessoa(
                null,
                request.nome(),
                request.cpf(),
                request.dataNascimento(),
                normalizeEmail(request.email())
        );

        pessoa = pessoaRepository.save(pessoa);
        return toResponse(pessoa);
    }

    @Transactional(readOnly = true)
    public PageResponse<PessoaResponse> findAll(String nome, String cpf, Pageable pageable) {
        Page<Pessoa> page = pessoaRepository.findAll(PessoaSpecification.filterBy(nome, cpf), pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PessoaResponse findById(UUID id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));
        return toResponse(pessoa);
    }

    @Transactional
    public PessoaResponse update(UUID id, PessoaUpdateRequest request) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));

        pessoaRepository.findByCpf(request.cpf())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("CPF já cadastrado");
                });

        pessoa.setNome(request.nome());
        pessoa.setCpf(request.cpf());
        pessoa.setDataNascimento(request.dataNascimento());
        pessoa.setEmail(normalizeEmail(request.email()));

        pessoa = pessoaRepository.save(pessoa);
        return toResponse(pessoa);
    }

    @Transactional
    public void delete(UUID id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));

        try {
            pessoaRepository.delete(pessoa);
            pessoaRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Não é possível excluir Pessoa com Beneficiários vinculados");
        }
    }

    private PessoaResponse toResponse(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getEmail(),
                pessoa.getCreatedAt(),
                pessoa.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return (email != null && !email.isBlank()) ? email.trim() : null;
    }
}

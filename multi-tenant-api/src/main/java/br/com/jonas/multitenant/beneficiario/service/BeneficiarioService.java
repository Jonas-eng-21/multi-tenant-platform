package br.com.jonas.multitenant.beneficiario.service;

import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioCreateRequest;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioResponse;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioUpdateRequest;
import br.com.jonas.multitenant.beneficiario.entity.Beneficiario;
import br.com.jonas.multitenant.beneficiario.repository.BeneficiarioRepository;
import br.com.jonas.multitenant.exception.ConflictException;
import br.com.jonas.multitenant.exception.ResourceNotFoundException;
import br.com.jonas.multitenant.pessoa.dto.PessoaResponse;
import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import br.com.jonas.multitenant.pessoa.repository.PessoaRepository;
import br.com.jonas.multitenant.security.TenantContext;
import br.com.jonas.multitenant.tenant.entity.Tenant;
import br.com.jonas.multitenant.tenant.repository.TenantRepository;
import br.com.jonas.multitenant.beneficiario.entity.StatusBeneficiario;
import br.com.jonas.multitenant.beneficiario.entity.TipoBeneficiario;
import br.com.jonas.multitenant.beneficiario.repository.BeneficiarioSpecification;
import br.com.jonas.multitenant.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final PessoaRepository pessoaRepository;
    private final TenantRepository tenantRepository;

    public BeneficiarioService(
            BeneficiarioRepository beneficiarioRepository,
            PessoaRepository pessoaRepository,
            TenantRepository tenantRepository
    ) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public BeneficiarioResponse create(BeneficiarioCreateRequest request) {
        UUID tenantId = getRequiredTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado"));

        Pessoa pessoa = pessoaRepository.findById(request.pessoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));

        beneficiarioRepository.findByTenantIdAndMatricula(tenantId, request.matricula())
                .ifPresent(b -> {
                    throw new ConflictException("Matrícula já cadastrada para este tenant");
                });

        Beneficiario beneficiario = new Beneficiario(
                null,
                pessoa,
                tenant,
                request.matricula(),
                request.tipo(),
                request.status(),
                request.dataAdesao()
        );

        beneficiario = beneficiarioRepository.save(beneficiario);
        return toResponse(beneficiario);
    }

    @Transactional(readOnly = true)
    public PageResponse<BeneficiarioResponse> findAll(String matricula, StatusBeneficiario status, TipoBeneficiario tipo, Pageable pageable) {
        UUID tenantId = getRequiredTenantId();

        Page<Beneficiario> page = beneficiarioRepository.findAll(
                BeneficiarioSpecification.filterBy(tenantId, matricula, status, tipo), 
                pageable
        );
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public BeneficiarioResponse findById(UUID id) {
        UUID tenantId = getRequiredTenantId();

        Beneficiario beneficiario = beneficiarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiário não encontrado"));

        return toResponse(beneficiario);
    }

    @Transactional
    public BeneficiarioResponse update(UUID id, BeneficiarioUpdateRequest request) {
        UUID tenantId = getRequiredTenantId();

        Beneficiario beneficiario = beneficiarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiário não encontrado"));

        beneficiarioRepository.findByTenantIdAndMatricula(tenantId, request.matricula())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Matrícula já cadastrada para este tenant");
                });

        beneficiario.setMatricula(request.matricula());
        beneficiario.setTipo(request.tipo());
        beneficiario.setStatus(request.status());
        beneficiario.setDataAdesao(request.dataAdesao());

        beneficiario = beneficiarioRepository.save(beneficiario);
        return toResponse(beneficiario);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = getRequiredTenantId();

        Beneficiario beneficiario = beneficiarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiário não encontrado"));

        beneficiarioRepository.delete(beneficiario);
        beneficiarioRepository.flush();
    }

    private UUID getRequiredTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant não identificado no contexto");
        }
        return tenantId;
    }

    private BeneficiarioResponse toResponse(Beneficiario beneficiario) {
        Pessoa pessoa = beneficiario.getPessoa();
        PessoaResponse pessoaResponse = new PessoaResponse(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getEmail(),
                pessoa.getCreatedAt(),
                pessoa.getUpdatedAt()
        );

        return new BeneficiarioResponse(
                beneficiario.getId(),
                pessoaResponse,
                beneficiario.getTenant().getId(),
                beneficiario.getMatricula(),
                beneficiario.getTipo(),
                beneficiario.getStatus(),
                beneficiario.getDataAdesao(),
                beneficiario.getCreatedAt(),
                beneficiario.getUpdatedAt()
        );
    }
}

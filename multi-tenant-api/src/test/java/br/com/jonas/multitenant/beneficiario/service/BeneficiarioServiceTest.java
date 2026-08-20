package br.com.jonas.multitenant.beneficiario.service;

import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioCreateRequest;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioResponse;
import br.com.jonas.multitenant.beneficiario.dto.BeneficiarioUpdateRequest;
import br.com.jonas.multitenant.beneficiario.entity.Beneficiario;
import br.com.jonas.multitenant.beneficiario.entity.StatusBeneficiario;
import br.com.jonas.multitenant.beneficiario.entity.TipoBeneficiario;
import br.com.jonas.multitenant.beneficiario.repository.BeneficiarioRepository;
import br.com.jonas.multitenant.exception.ConflictException;
import br.com.jonas.multitenant.exception.ResourceNotFoundException;
import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import br.com.jonas.multitenant.pessoa.repository.PessoaRepository;
import br.com.jonas.multitenant.security.TenantContext;
import br.com.jonas.multitenant.tenant.entity.Tenant;
import br.com.jonas.multitenant.tenant.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import br.com.jonas.multitenant.common.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiarioServiceTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private BeneficiarioService beneficiarioService;

    private final UUID tenant1Id = UUID.randomUUID();
    private final UUID tenant2Id = UUID.randomUUID();
    private final UUID pessoaId = UUID.randomUUID();
    private final UUID beneficiarioId = UUID.randomUUID();

    private Tenant tenant1;
    private Tenant tenant2;
    private Pessoa pessoa;

    @BeforeEach
    void setUp() {
        tenant1 = new Tenant(tenant1Id, "Tenant 1");
        tenant2 = new Tenant(tenant2Id, "Tenant 2");
        pessoa = new Pessoa(pessoaId, "Carlos Silva", "12345678901", LocalDate.of(1985, 5, 10), "carlos@email.com");
        TenantContext.setTenantId(tenant1Id);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_associatesWithTenantFromContext_success() {
        BeneficiarioCreateRequest request = new BeneficiarioCreateRequest(
                pessoaId,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        Beneficiario beneficiarioSalvo = new Beneficiario(
                beneficiarioId,
                pessoa,
                tenant1,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        when(tenantRepository.findById(tenant1Id)).thenReturn(Optional.of(tenant1));
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(beneficiarioRepository.findByTenantIdAndMatricula(tenant1Id, "MAT-001")).thenReturn(Optional.empty());
        when(beneficiarioRepository.save(any(Beneficiario.class))).thenReturn(beneficiarioSalvo);

        BeneficiarioResponse response = beneficiarioService.create(request);

        assertNotNull(response);
        assertEquals(beneficiarioId, response.id());
        assertEquals(tenant1Id, response.tenantId());
        assertEquals("MAT-001", response.matricula());
        assertEquals(pessoaId, response.pessoa().id());
        verify(beneficiarioRepository).save(any(Beneficiario.class));
    }

    @Test
    void findAll_returnsOnlyCurrentTenantRecords() {
        Pageable pageable = PageRequest.of(0, 10);
        Beneficiario b1 = new Beneficiario(
                beneficiarioId,
                pessoa,
                tenant1,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        when(beneficiarioRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(b1)));

        PageResponse<BeneficiarioResponse> result = beneficiarioService.findAll(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(beneficiarioId, result.content().get(0).id());
        verify(beneficiarioRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    void findById_fromDifferentTenant_throwsNotFound() {
        when(beneficiarioRepository.findByIdAndTenantId(beneficiarioId, tenant1Id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiarioService.findById(beneficiarioId));
        verify(beneficiarioRepository).findByIdAndTenantId(beneficiarioId, tenant1Id);
    }

    @Test
    void update_fromDifferentTenant_throwsNotFound() {
        BeneficiarioUpdateRequest request = new BeneficiarioUpdateRequest(
                "MAT-002",
                TipoBeneficiario.DEPENDENTE,
                StatusBeneficiario.INATIVO,
                LocalDate.now()
        );

        when(beneficiarioRepository.findByIdAndTenantId(beneficiarioId, tenant1Id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiarioService.update(beneficiarioId, request));
        verify(beneficiarioRepository, never()).save(any(Beneficiario.class));
    }

    @Test
    void delete_fromDifferentTenant_throwsNotFound() {
        when(beneficiarioRepository.findByIdAndTenantId(beneficiarioId, tenant1Id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiarioService.delete(beneficiarioId));
        verify(beneficiarioRepository, never()).delete(any(Beneficiario.class));
    }

    @Test
    void create_duplicateMatriculaInSameTenant_throwsConflictException() {
        BeneficiarioCreateRequest request = new BeneficiarioCreateRequest(
                pessoaId,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        Beneficiario existing = new Beneficiario(
                UUID.randomUUID(),
                pessoa,
                tenant1,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        when(tenantRepository.findById(tenant1Id)).thenReturn(Optional.of(tenant1));
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(beneficiarioRepository.findByTenantIdAndMatricula(tenant1Id, "MAT-001")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> beneficiarioService.create(request));
        verify(beneficiarioRepository, never()).save(any(Beneficiario.class));
    }

    @Test
    void create_sameMatriculaInDifferentTenants_isAllowed() {
        BeneficiarioCreateRequest request = new BeneficiarioCreateRequest(
                pessoaId,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        // Switch to Tenant 2
        TenantContext.setTenantId(tenant2Id);

        Beneficiario beneficiarioTenant2 = new Beneficiario(
                beneficiarioId,
                pessoa,
                tenant2,
                "MAT-001",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        when(tenantRepository.findById(tenant2Id)).thenReturn(Optional.of(tenant2));
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(beneficiarioRepository.findByTenantIdAndMatricula(tenant2Id, "MAT-001")).thenReturn(Optional.empty());
        when(beneficiarioRepository.save(any(Beneficiario.class))).thenReturn(beneficiarioTenant2);

        BeneficiarioResponse response = beneficiarioService.create(request);

        assertNotNull(response);
        assertEquals(tenant2Id, response.tenantId());
        assertEquals("MAT-001", response.matricula());
        verify(beneficiarioRepository).save(any(Beneficiario.class));
    }

    @Test
    void create_samePessoaInDifferentTenants_isAllowed() {
        BeneficiarioCreateRequest requestTenant1 = new BeneficiarioCreateRequest(
                pessoaId,
                "MAT-T1",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        Beneficiario bTenant1 = new Beneficiario(
                UUID.randomUUID(),
                pessoa,
                tenant1,
                "MAT-T1",
                TipoBeneficiario.TITULAR,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        when(tenantRepository.findById(tenant1Id)).thenReturn(Optional.of(tenant1));
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(beneficiarioRepository.findByTenantIdAndMatricula(tenant1Id, "MAT-T1")).thenReturn(Optional.empty());
        when(beneficiarioRepository.save(any(Beneficiario.class))).thenReturn(bTenant1);

        BeneficiarioResponse response1 = beneficiarioService.create(requestTenant1);
        assertEquals(tenant1Id, response1.tenantId());
        assertEquals(pessoaId, response1.pessoa().id());

        // Switch to Tenant 2
        TenantContext.setTenantId(tenant2Id);

        BeneficiarioCreateRequest requestTenant2 = new BeneficiarioCreateRequest(
                pessoaId,
                "MAT-T2",
                TipoBeneficiario.DEPENDENTE,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        Beneficiario bTenant2 = new Beneficiario(
                UUID.randomUUID(),
                pessoa,
                tenant2,
                "MAT-T2",
                TipoBeneficiario.DEPENDENTE,
                StatusBeneficiario.ATIVO,
                LocalDate.now()
        );

        when(tenantRepository.findById(tenant2Id)).thenReturn(Optional.of(tenant2));
        when(beneficiarioRepository.findByTenantIdAndMatricula(tenant2Id, "MAT-T2")).thenReturn(Optional.empty());
        when(beneficiarioRepository.save(any(Beneficiario.class))).thenReturn(bTenant2);

        BeneficiarioResponse response2 = beneficiarioService.create(requestTenant2);
        assertEquals(tenant2Id, response2.tenantId());
        assertEquals(pessoaId, response2.pessoa().id());
    }
}

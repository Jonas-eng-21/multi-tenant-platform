package br.com.jonas.multitenant.pessoa.service;

import br.com.jonas.multitenant.exception.ConflictException;
import br.com.jonas.multitenant.exception.ResourceNotFoundException;
import br.com.jonas.multitenant.pessoa.dto.PessoaCreateRequest;
import br.com.jonas.multitenant.pessoa.dto.PessoaResponse;
import br.com.jonas.multitenant.pessoa.dto.PessoaUpdateRequest;
import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import br.com.jonas.multitenant.pessoa.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private PessoaService pessoaService;

    private final UUID pessoaId = UUID.randomUUID();

    @Test
    void create_validRequest_returnsResponse() {
        PessoaCreateRequest request = new PessoaCreateRequest("João", "12345678901", LocalDate.of(1990, 1, 1), "joao@email.com");
        Pessoa pessoaSalva = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), "joao@email.com");

        when(pessoaRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoaSalva);

        PessoaResponse response = pessoaService.create(request);

        assertNotNull(response);
        assertEquals(pessoaId, response.id());
        assertEquals("João", response.nome());
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    @Test
    void findAll_returnsPageResponse() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Pessoa p = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), "joao@email.com");
        
        when(pessoaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(p)));

        br.com.jonas.multitenant.common.dto.PageResponse<PessoaResponse> result = pessoaService.findAll(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("João", result.content().get(0).nome());
        verify(pessoaRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    void create_cpfAlreadyExists_throwsConflictException() {
        PessoaCreateRequest request = new PessoaCreateRequest("João", "12345678901", LocalDate.of(1990, 1, 1), "joao@email.com");
        Pessoa pessoaExistente = new Pessoa(pessoaId, "Maria", "12345678901", LocalDate.of(1990, 1, 1), null);

        when(pessoaRepository.findByCpf("12345678901")).thenReturn(Optional.of(pessoaExistente));

        assertThrows(ConflictException.class, () -> pessoaService.create(request));
        verify(pessoaRepository, never()).save(any(Pessoa.class));
    }

    @Test
    void findById_exists_returnsResponse() {
        Pessoa pessoa = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), null);
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));

        PessoaResponse response = pessoaService.findById(pessoaId);

        assertNotNull(response);
        assertEquals(pessoaId, response.id());
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pessoaService.findById(pessoaId));
    }

    @Test
    void update_validRequest_returnsUpdatedResponse() {
        PessoaUpdateRequest request = new PessoaUpdateRequest("João Atualizado", "12345678901", LocalDate.of(1990, 1, 1), "novo@email.com");
        Pessoa pessoa = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), null);
        
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.findByCpf("12345678901")).thenReturn(Optional.of(pessoa)); // Same CPF but same ID, so it's valid
        when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

        PessoaResponse response = pessoaService.update(pessoaId, request);

        assertEquals("João Atualizado", response.nome());
        assertEquals("novo@email.com", response.email());
        verify(pessoaRepository).save(pessoa);
    }

    @Test
    void update_cpfBelongsToAnotherPessoa_throwsConflictException() {
        PessoaUpdateRequest request = new PessoaUpdateRequest("João Atualizado", "10987654321", LocalDate.of(1990, 1, 1), null);
        Pessoa pessoa = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), null);
        
        UUID outraPessoaId = UUID.randomUUID();
        Pessoa outraPessoa = new Pessoa(outraPessoaId, "Maria", "10987654321", LocalDate.of(1992, 1, 1), null);

        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.findByCpf("10987654321")).thenReturn(Optional.of(outraPessoa));

        assertThrows(ConflictException.class, () -> pessoaService.update(pessoaId, request));
        verify(pessoaRepository, never()).save(any(Pessoa.class));
    }

    @Test
    void delete_exists_deletesPessoa() {
        Pessoa pessoa = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), null);
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));

        pessoaService.delete(pessoaId);

        verify(pessoaRepository).delete(pessoa);
        verify(pessoaRepository).flush();
    }

    @Test
    void delete_hasBeneficiarios_throwsConflictException() {
        Pessoa pessoa = new Pessoa(pessoaId, "João", "12345678901", LocalDate.of(1990, 1, 1), null);
        when(pessoaRepository.findById(pessoaId)).thenReturn(Optional.of(pessoa));
        doThrow(DataIntegrityViolationException.class).when(pessoaRepository).flush();

        assertThrows(ConflictException.class, () -> pessoaService.delete(pessoaId));
    }
}

package br.com.jonas.multitenant.beneficiario.repository;

import br.com.jonas.multitenant.beneficiario.entity.Beneficiario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, UUID>, JpaSpecificationExecutor<Beneficiario> {

    Optional<Beneficiario> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Beneficiario> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Beneficiario> findByTenantIdAndMatricula(UUID tenantId, String matricula);

    Page<Beneficiario> findByTenantIdAndMatriculaContainingIgnoreCase(UUID tenantId, String matricula, Pageable pageable);
}


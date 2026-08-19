package br.com.jonas.multitenant.tenant.repository;

import br.com.jonas.multitenant.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
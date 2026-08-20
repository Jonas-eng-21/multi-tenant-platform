package br.com.jonas.multitenant.beneficiario.repository;

import br.com.jonas.multitenant.beneficiario.entity.Beneficiario;
import br.com.jonas.multitenant.beneficiario.entity.StatusBeneficiario;
import br.com.jonas.multitenant.beneficiario.entity.TipoBeneficiario;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class BeneficiarioSpecification {

    private BeneficiarioSpecification() {
    }

    public static Specification<Beneficiario> filterBy(UUID tenantId, String matricula, StatusBeneficiario status, TipoBeneficiario tipo) {
        return Specification
                .where(hasTenantId(tenantId))
                .and(hasMatricula(matricula))
                .and(hasStatus(status))
                .and(hasTipo(tipo));
    }

    private static Specification<Beneficiario> hasTenantId(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    private static Specification<Beneficiario> hasMatricula(String matricula) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(matricula)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("matricula")), "%" + matricula.toLowerCase() + "%");
        };
    }

    private static Specification<Beneficiario> hasStatus(StatusBeneficiario status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<Beneficiario> hasTipo(TipoBeneficiario tipo) {
        return (root, query, cb) -> {
            if (tipo == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("tipo"), tipo);
        };
    }
}

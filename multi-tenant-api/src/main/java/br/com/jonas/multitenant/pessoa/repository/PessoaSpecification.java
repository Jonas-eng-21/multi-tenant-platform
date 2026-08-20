package br.com.jonas.multitenant.pessoa.repository;

import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PessoaSpecification {

    private PessoaSpecification() {
    }

    public static Specification<Pessoa> filterBy(String nome, String cpf) {
        return Specification
                .where(hasNome(nome))
                .and(hasCpf(cpf));
    }

    private static Specification<Pessoa> hasNome(String nome) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(nome)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
        };
    }

    private static Specification<Pessoa> hasCpf(String cpf) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(cpf)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("cpf"), cpf);
        };
    }
}

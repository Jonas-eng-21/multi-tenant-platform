package br.com.jonas.multitenant.pessoa.repository;

import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {

    Optional<Pessoa> findByCpf(String cpf);

    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}

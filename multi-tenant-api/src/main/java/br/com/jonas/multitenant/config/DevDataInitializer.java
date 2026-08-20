package br.com.jonas.multitenant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private static final UUID TENANT_A_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TENANT_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TENANT_C_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID USER_A_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_B_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID USER_C_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private static final UUID PESSOA_JOAO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID PESSOA_MARIA_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID PESSOA_CARLOS_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID PESSOA_ANA_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");

    private static final UUID BEN_JOAO_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID BEN_MARIA_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID BEN_MARIA_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final UUID BEN_CARLOS_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000004");
    private static final UUID BEN_CARLOS_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000005");
    private static final UUID BEN_CARLOS_C_ID = UUID.fromString("20000000-0000-0000-0000-000000000006");
    private static final UUID BEN_ANA_C_ID = UUID.fromString("20000000-0000-0000-0000-000000000007");

    private static final String CPF_JOAO = "52998224725";
    private static final String CPF_MARIA = "01934587648";
    private static final String CPF_CARLOS = "84756321062";
    private static final String CPF_ANA = "36521479016";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.password:}")
    private String seedPassword;

    public DevDataInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (seedPassword == null || seedPassword.isBlank()) {
            log.warn("[DEV SEED] SEED_PASSWORD não configurada. Seed não executado.");
            return;
        }

        log.info("[DEV SEED] Iniciando seed de dados de desenvolvimento...");

        String encodedPassword = passwordEncoder.encode(seedPassword);

        createTenants();
        createUsers(encodedPassword);
        createPessoas();
        createBeneficiarios();

        log.info("[DEV SEED] Seed de dados de desenvolvimento concluído.");
    }

    private void createTenants() {
        insertTenantIfAbsent(TENANT_A_ID, "Empresa Alpha");
        insertTenantIfAbsent(TENANT_B_ID, "Empresa Beta");
        insertTenantIfAbsent(TENANT_C_ID, "Empresa Gama");
    }

    private void createUsers(String encodedPassword) {
        insertUserIfAbsent(USER_A_ID, TENANT_A_ID, "admin", encodedPassword);
        insertUserIfAbsent(USER_B_ID, TENANT_B_ID, "admin", encodedPassword);
        insertUserIfAbsent(USER_C_ID, TENANT_C_ID, "admin", encodedPassword);
    }

    private void createPessoas() {
        insertPessoaIfAbsent(PESSOA_JOAO_ID, "João Silva", CPF_JOAO,
                LocalDate.of(1990, 3, 15), "joao.silva@teste.dev");
        insertPessoaIfAbsent(PESSOA_MARIA_ID, "Maria Santos", CPF_MARIA,
                LocalDate.of(1985, 7, 22), "maria.santos@teste.dev");
        insertPessoaIfAbsent(PESSOA_CARLOS_ID, "Carlos Oliveira", CPF_CARLOS,
                LocalDate.of(1978, 11, 8), "carlos.oliveira@teste.dev");
        insertPessoaIfAbsent(PESSOA_ANA_ID, "Ana Costa", CPF_ANA,
                LocalDate.of(1995, 1, 30), "ana.costa@teste.dev");
    }

    private void createBeneficiarios() {
        insertBeneficiarioIfAbsent(BEN_JOAO_A_ID, PESSOA_JOAO_ID, TENANT_A_ID,
                "MAT-100", "TITULAR", "ATIVO", LocalDate.of(2024, 1, 15));

        insertBeneficiarioIfAbsent(BEN_MARIA_A_ID, PESSOA_MARIA_ID, TENANT_A_ID,
                "MAT-101", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 2, 1));

        insertBeneficiarioIfAbsent(BEN_MARIA_B_ID, PESSOA_MARIA_ID, TENANT_B_ID,
                "MAT-200", "TITULAR", "ATIVO", LocalDate.of(2024, 3, 10));

        insertBeneficiarioIfAbsent(BEN_CARLOS_A_ID, PESSOA_CARLOS_ID, TENANT_A_ID,
                "MAT-102", "DEPENDENTE", "INATIVO", LocalDate.of(2023, 6, 20));

        insertBeneficiarioIfAbsent(BEN_CARLOS_B_ID, PESSOA_CARLOS_ID, TENANT_B_ID,
                "MAT-100", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 4, 5));

        insertBeneficiarioIfAbsent(BEN_CARLOS_C_ID, PESSOA_CARLOS_ID, TENANT_C_ID,
                "MAT-300", "TITULAR", "ATIVO", LocalDate.of(2024, 5, 12));

        insertBeneficiarioIfAbsent(BEN_ANA_C_ID, PESSOA_ANA_ID, TENANT_C_ID,
                "MAT-301", "TITULAR", "ATIVO", LocalDate.of(2024, 6, 1));
    }


    private void insertTenantIfAbsent(UUID id, String nome) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tenants WHERE id = ?", Integer.class, id
        );
        if (count != null && count > 0) {
            log.debug("[DEV SEED] Tenant {} já existe, ignorando.", nome);
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO tenants (id, nome, created_at) VALUES (?, ?, ?)",
                id, nome, Timestamp.from(Instant.now())
        );
        log.info("[DEV SEED] Tenant criado: {} ({})", nome, id);
    }

    private void insertUserIfAbsent(UUID id, UUID tenantId, String username, String encodedPassword) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id
        );
        if (count != null && count > 0) {
            log.debug("[DEV SEED] User {} já existe, ignorando.", username);
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO users (id, tenant_id, username, password, created_at) VALUES (?, ?, ?, ?, ?)",
                id, tenantId, username, encodedPassword, Timestamp.from(Instant.now())
        );
        log.info("[DEV SEED] User criado: {} no tenant {}", username, tenantId);
    }

    private void insertPessoaIfAbsent(UUID id, String nome, String cpf, LocalDate dataNascimento, String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pessoas WHERE id = ?", Integer.class, id
        );
        if (count != null && count > 0) {
            log.debug("[DEV SEED] Pessoa {} já existe, ignorando.", nome);
            return;
        }
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
                "INSERT INTO pessoas (id, nome, cpf, data_nascimento, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, nome, cpf, Date.valueOf(dataNascimento), email, now, now
        );
        log.info("[DEV SEED] Pessoa criada: {} ({})", nome, id);
    }

    private void insertBeneficiarioIfAbsent(UUID id, UUID pessoaId, UUID tenantId,
                                            String matricula, String tipo,
                                            String status, LocalDate dataAdesao) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM beneficiarios WHERE id = ?", Integer.class, id
        );
        if (count != null && count > 0) {
            log.debug("[DEV SEED] Beneficiário {} já existe, ignorando.", id);
            return;
        }
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(
                "INSERT INTO beneficiarios (id, pessoa_id, tenant_id, matricula, tipo, status, data_adesao, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, pessoaId, tenantId, matricula, tipo, status, Date.valueOf(dataAdesao), now, now
        );
        log.info("[DEV SEED] Beneficiário criado: ID {}, Matrícula {}, Tenant {}", id, matricula, tenantId);
    }
}

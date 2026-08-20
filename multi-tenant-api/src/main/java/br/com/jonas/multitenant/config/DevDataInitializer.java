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

    private static final UUID TENANT_A_ID = UUID.fromString("d1111111-1111-1111-1111-111111111111");
    private static final UUID TENANT_B_ID = UUID.fromString("d2222222-2222-2222-2222-222222222222");
    private static final UUID TENANT_C_ID = UUID.fromString("d3333333-3333-3333-3333-333333333333");

    private static final UUID USER_A_ID = UUID.fromString("daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_B_ID = UUID.fromString("dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID USER_C_ID = UUID.fromString("dccccccc-cccc-cccc-cccc-cccccccccccc");

    private static final UUID P1_ID = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID P2_ID = UUID.fromString("e1000000-0000-0000-0000-000000000002");
    private static final UUID P3_ID = UUID.fromString("e1000000-0000-0000-0000-000000000003");
    private static final UUID P4_ID = UUID.fromString("e1000000-0000-0000-0000-000000000004");
    private static final UUID P5_ID = UUID.fromString("e1000000-0000-0000-0000-000000000005");
    private static final UUID P6_ID = UUID.fromString("e1000000-0000-0000-0000-000000000006");
    private static final UUID P7_ID = UUID.fromString("e1000000-0000-0000-0000-000000000007");
    private static final UUID P8_ID = UUID.fromString("e1000000-0000-0000-0000-000000000008");
    private static final UUID P9_ID = UUID.fromString("e1000000-0000-0000-0000-000000000009");
    private static final UUID P10_ID = UUID.fromString("e1000000-0000-0000-0000-000000000010");

    private static final String CPF_P1 = "71428793060";
    private static final String CPF_P2 = "04373432091";
    private static final String CPF_P3 = "98763567090";
    private static final String CPF_P4 = "92716388057";
    private static final String CPF_P5 = "11904727042";
    private static final String CPF_P6 = "85741630044";
    private static final String CPF_P7 = "25270133036";
    private static final String CPF_P8 = "28169123019";
    private static final String CPF_P9 = "95244517006";
    private static final String CPF_P10 = "61286345091";

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

        log.info("[DEV SEED] Seed de dados de desenvolvimento concluído com sucesso.");
    }

    private void createTenants() {
        insertTenantIfAbsent(TENANT_A_ID, "Tenant Dev A");
        insertTenantIfAbsent(TENANT_B_ID, "Tenant Dev B");
        insertTenantIfAbsent(TENANT_C_ID, "Tenant Dev C");
    }

    private void createUsers(String encodedPassword) {
        insertUserIfAbsent(USER_A_ID, TENANT_A_ID, "admin-a", encodedPassword);
        insertUserIfAbsent(USER_B_ID, TENANT_B_ID, "admin-b", encodedPassword);
        insertUserIfAbsent(USER_C_ID, TENANT_C_ID, "admin-c", encodedPassword);
    }

    private void createPessoas() {
        insertPessoaIfAbsent(P1_ID, "Arthur Aguiar Dev", CPF_P1, LocalDate.of(1990, 5, 20), "arthur.dev@teste.com");
        insertPessoaIfAbsent(P2_ID, "Bruna Bittencourt Dev", CPF_P2, LocalDate.of(1985, 3, 15), "bruna.dev@teste.com");
        insertPessoaIfAbsent(P3_ID, "Caio Castro Dev", CPF_P3, LocalDate.of(1992, 11, 2), "caio.dev@teste.com");
        insertPessoaIfAbsent(P4_ID, "Daniela Duarte Dev", CPF_P4, LocalDate.of(1978, 8, 24), "daniela.dev@teste.com");
        insertPessoaIfAbsent(P5_ID, "Eduardo Elias Dev", CPF_P5, LocalDate.of(2000, 1, 10), "eduardo.dev@teste.com");
        insertPessoaIfAbsent(P6_ID, "Fernanda Fernandes Dev", CPF_P6, LocalDate.of(1996, 7, 18), "fernanda.dev@teste.com");
        insertPessoaIfAbsent(P7_ID, "Gustavo Gomes Dev", CPF_P7, LocalDate.of(1988, 12, 30), "gustavo.dev@teste.com");
        insertPessoaIfAbsent(P8_ID, "Helena Holanda Dev", CPF_P8, LocalDate.of(1994, 2, 28), "helena.dev@teste.com");
        insertPessoaIfAbsent(P9_ID, "Igor Inácio Dev", CPF_P9, LocalDate.of(1982, 9, 14), "igor.dev@teste.com");
        insertPessoaIfAbsent(P10_ID, "Juliana Junqueira Dev", CPF_P10, LocalDate.of(1975, 4, 5), "juliana.dev@teste.com");
    }

    private void createBeneficiarios() {
        String baseBenId = "f2000000-0000-0000-0000-0000000000";

        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "01"), P1_ID, TENANT_A_ID, "DEV-A-100", "TITULAR", "ATIVO", LocalDate.of(2024, 1, 10));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "02"), P2_ID, TENANT_A_ID, "DEV-A-101", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 1, 12));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "03"), P3_ID, TENANT_A_ID, "DEV-A-102", "TITULAR", "INATIVO", LocalDate.of(2023, 5, 20));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "04"), P4_ID, TENANT_A_ID, "DEV-A-103", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 2, 5));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "05"), P5_ID, TENANT_A_ID, "DEV-A-104", "TITULAR", "ATIVO", LocalDate.of(2024, 3, 1));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "06"), P6_ID, TENANT_A_ID, "DEV-A-105", "DEPENDENTE", "INATIVO", LocalDate.of(2022, 10, 15));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "07"), P7_ID, TENANT_A_ID, "DEV-A-106", "TITULAR", "ATIVO", LocalDate.of(2024, 4, 20));


        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "08"), P1_ID, TENANT_B_ID, "DEV-B-200", "TITULAR", "ATIVO", LocalDate.of(2024, 1, 15));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "09"), P2_ID, TENANT_B_ID, "DEV-B-201", "TITULAR", "INATIVO", LocalDate.of(2023, 8, 10));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "10"), P7_ID, TENANT_B_ID, "DEV-B-202", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 5, 5));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "11"), P8_ID, TENANT_B_ID, "DEV-B-203", "TITULAR", "ATIVO", LocalDate.of(2024, 5, 10));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "12"), P9_ID, TENANT_B_ID, "DEV-B-204", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 6, 12));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "13"), P10_ID, TENANT_B_ID, "DEV-B-205", "TITULAR", "ATIVO", LocalDate.of(2024, 7, 1));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "14"), P3_ID, TENANT_B_ID, "DEV-B-206", "DEPENDENTE", "INATIVO", LocalDate.of(2023, 12, 1));

        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "15"), P9_ID, TENANT_C_ID, "DEV-C-300", "TITULAR", "ATIVO", LocalDate.of(2024, 2, 20));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "16"), P10_ID, TENANT_C_ID, "DEV-C-301", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 3, 25));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "17"), P4_ID, TENANT_C_ID, "DEV-C-302", "TITULAR", "INATIVO", LocalDate.of(2022, 11, 10));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "18"), P5_ID, TENANT_C_ID, "DEV-C-303", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 4, 15));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "19"), P6_ID, TENANT_C_ID, "DEV-C-304", "TITULAR", "ATIVO", LocalDate.of(2024, 5, 30));
        insertBeneficiarioIfAbsent(UUID.fromString(baseBenId + "20"), P8_ID, TENANT_C_ID, "DEV-C-305", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 6, 20));
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
            log.debug("[DEV SEED] Pessoa {} (ID {}) já existe, ignorando.", nome, id);
            return;
        }
        
        Integer countCpf = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pessoas WHERE cpf = ?", Integer.class, cpf
        );
        if (countCpf != null && countCpf > 0) {
             log.warn("[DEV SEED] Pessoa {} não inserida: O CPF {} já existe sob outro ID na base.", nome, cpf);
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
        
        Integer countMatricula = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM beneficiarios WHERE tenant_id = ? AND matricula = ?", Integer.class, tenantId, matricula
        );
        if (countMatricula != null && countMatricula > 0) {
             log.warn("[DEV SEED] Beneficiário não inserido: A Matrícula {} já existe no tenant {}.", matricula, tenantId);
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

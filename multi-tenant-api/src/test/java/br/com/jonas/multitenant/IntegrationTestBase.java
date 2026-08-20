package br.com.jonas.multitenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
public abstract class IntegrationTestBase {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;


    protected ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected static final UUID TENANT_A_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    protected static final UUID TENANT_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    protected static final UUID TENANT_C_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    protected static final UUID USER_A_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    protected static final UUID USER_B_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    protected static final UUID USER_C_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    protected static final UUID PESSOA_JOAO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    protected static final UUID PESSOA_MARIA_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    protected static final UUID PESSOA_CARLOS_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    protected static final UUID PESSOA_ANA_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");

    protected static final UUID BEN_JOAO_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    protected static final UUID BEN_MARIA_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    protected static final UUID BEN_MARIA_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    protected static final UUID BEN_CARLOS_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000004");
    protected static final UUID BEN_CARLOS_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000005");
    protected static final UUID BEN_CARLOS_C_ID = UUID.fromString("20000000-0000-0000-0000-000000000006");
    protected static final UUID BEN_ANA_C_ID = UUID.fromString("20000000-0000-0000-0000-000000000007");

    protected final String TEST_PASSWORD = "password123";

    @BeforeEach
    public void setupFixtures() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        cleanDatabase();
        String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);

        // Insert Tenants
        jdbcTemplate.update("INSERT INTO tenants (id, nome, created_at) VALUES (?, ?, ?)",
                TENANT_A_ID, "Tenant A", Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO tenants (id, nome, created_at) VALUES (?, ?, ?)",
                TENANT_B_ID, "Tenant B", Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO tenants (id, nome, created_at) VALUES (?, ?, ?)",
                TENANT_C_ID, "Tenant C", Timestamp.from(Instant.now()));

        // Insert Users
        jdbcTemplate.update("INSERT INTO users (id, tenant_id, username, password, created_at) VALUES (?, ?, ?, ?, ?)",
                USER_A_ID, TENANT_A_ID, "admin", encodedPassword, Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO users (id, tenant_id, username, password, created_at) VALUES (?, ?, ?, ?, ?)",
                USER_B_ID, TENANT_B_ID, "admin", encodedPassword, Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO users (id, tenant_id, username, password, created_at) VALUES (?, ?, ?, ?, ?)",
                USER_C_ID, TENANT_C_ID, "admin", encodedPassword, Timestamp.from(Instant.now()));

        // Insert Pessoas
        jdbcTemplate.update("INSERT INTO pessoas (id, nome, cpf, data_nascimento, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                PESSOA_JOAO_ID, "João Silva", "52998224725", Date.valueOf(LocalDate.of(1990, 3, 15)), "joao@teste.dev", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO pessoas (id, nome, cpf, data_nascimento, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                PESSOA_MARIA_ID, "Maria Santos", "01934587648", Date.valueOf(LocalDate.of(1985, 7, 22)), "maria@teste.dev", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO pessoas (id, nome, cpf, data_nascimento, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                PESSOA_CARLOS_ID, "Carlos Oliveira", "84756321062", Date.valueOf(LocalDate.of(1978, 11, 8)), "carlos@teste.dev", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        jdbcTemplate.update("INSERT INTO pessoas (id, nome, cpf, data_nascimento, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                PESSOA_ANA_ID, "Ana Costa", "36521479016", Date.valueOf(LocalDate.of(1995, 1, 30)), "ana@teste.dev", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));

        // Insert Beneficiarios
        insertBeneficiario(BEN_JOAO_A_ID, PESSOA_JOAO_ID, TENANT_A_ID, "MAT-100", "TITULAR", "ATIVO", LocalDate.of(2024, 1, 15));
        insertBeneficiario(BEN_MARIA_A_ID, PESSOA_MARIA_ID, TENANT_A_ID, "MAT-101", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 2, 1));
        insertBeneficiario(BEN_MARIA_B_ID, PESSOA_MARIA_ID, TENANT_B_ID, "MAT-200", "TITULAR", "ATIVO", LocalDate.of(2024, 3, 10));
        insertBeneficiario(BEN_CARLOS_A_ID, PESSOA_CARLOS_ID, TENANT_A_ID, "MAT-102", "DEPENDENTE", "INATIVO", LocalDate.of(2023, 6, 20));
        insertBeneficiario(BEN_CARLOS_B_ID, PESSOA_CARLOS_ID, TENANT_B_ID, "MAT-100", "DEPENDENTE", "ATIVO", LocalDate.of(2024, 4, 5));
        insertBeneficiario(BEN_CARLOS_C_ID, PESSOA_CARLOS_ID, TENANT_C_ID, "MAT-300", "TITULAR", "ATIVO", LocalDate.of(2024, 5, 12));
        insertBeneficiario(BEN_ANA_C_ID, PESSOA_ANA_ID, TENANT_C_ID, "MAT-301", "TITULAR", "ATIVO", LocalDate.of(2024, 6, 1));
    }

    private void insertBeneficiario(UUID id, UUID pessoaId, UUID tenantId, String matricula, String tipo, String status, LocalDate dataAdesao) {
        jdbcTemplate.update("INSERT INTO beneficiarios (id, pessoa_id, tenant_id, matricula, tipo, status, data_adesao, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, pessoaId, tenantId, matricula, tipo, status, Date.valueOf(dataAdesao), Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
    }

    private void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM beneficiarios");
        jdbcTemplate.execute("DELETE FROM pessoas");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM tenants");
    }

    protected String getAuthToken(UUID tenantId, String username, String password) throws Exception {
        String jsonPayload = String.format("{\"tenantId\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"}", tenantId, username, password);
        
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}

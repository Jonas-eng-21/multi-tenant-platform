package br.com.jonas.multitenant;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void login_WithValidCredentials_Returns200AndToken() throws Exception {
        String jsonPayload = String.format("{\"tenantId\":\"%s\",\"username\":\"admin\",\"password\":\"%s\"}", TENANT_A_ID, TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_WithInvalidPassword_Returns401() throws Exception {
        String jsonPayload = String.format("{\"tenantId\":\"%s\",\"username\":\"admin\",\"password\":\"wrongpassword\"}", TENANT_A_ID);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas ou token de acesso ausente/expirado"));
    }

    @Test
    void login_WithInvalidUsername_Returns401() throws Exception {
        String jsonPayload = String.format("{\"tenantId\":\"%s\",\"username\":\"wronguser\",\"password\":\"%s\"}", TENANT_A_ID, TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas ou token de acesso ausente/expirado"));
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/beneficiarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/beneficiarios")
                .header("Authorization", "Bearer invalid-token.abcd.1234"))
                .andExpect(status().isUnauthorized());
    }
}

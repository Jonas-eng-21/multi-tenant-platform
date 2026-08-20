package br.com.jonas.multitenant;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaginationIntegrationTest extends IntegrationTestBase {

    @Test
    void listPessoas_WithPaginationAndFilters_ReturnsCorrectPage() throws Exception {
        String token = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/pessoas?page=0&size=2&sort=nome,asc")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome").value("Ana Costa"))
                .andExpect(jsonPath("$.content[1].nome").value("Carlos Oliveira"))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void listPessoas_WithCpfFilter_ReturnsSingleResult() throws Exception {
        String token = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/pessoas?cpf=52998224725")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value("João Silva"));
    }

    @Test
    void listBeneficiarios_WithFilters_MaintainsTenantIsolation() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        // Tenant A searching for ATIVO
        mockMvc.perform(get("/api/beneficiarios?status=ATIVO&sort=matricula,desc")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].matricula").value("MAT-101"))
                .andExpect(jsonPath("$.content[1].matricula").value("MAT-100"));
    }

    @Test
    void listBeneficiarios_WithInvalidSortField_Returns400() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/beneficiarios?sort=campoQueNaoExiste,asc")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Propriedade de ordenação inválida: 'campoQueNaoExiste'"));
    }
}

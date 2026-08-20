package br.com.jonas.multitenant;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MultiTenantIsolationIntegrationTest extends IntegrationTestBase {

    @Test
    void listBeneficiarios_TenantA_ReturnsOnlyTenantA() throws Exception {
        String token = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/beneficiarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].tenantId").value(TENANT_A_ID.toString()))
                .andExpect(jsonPath("$.content[1].tenantId").value(TENANT_A_ID.toString()))
                .andExpect(jsonPath("$.content[2].tenantId").value(TENANT_A_ID.toString()));
    }

    @Test
    void listBeneficiarios_TenantB_ReturnsOnlyTenantB() throws Exception {
        String token = getAuthToken(TENANT_B_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/beneficiarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tenantId").value(TENANT_B_ID.toString()))
                .andExpect(jsonPath("$.content[1].tenantId").value(TENANT_B_ID.toString()));
    }

    @Test
    void listBeneficiarios_TenantC_ReturnsOnlyTenantC() throws Exception {
        String token = getAuthToken(TENANT_C_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/beneficiarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tenantId").value(TENANT_C_ID.toString()))
                .andExpect(jsonPath("$.content[1].tenantId").value(TENANT_C_ID.toString()));
    }

    @Test
    void getBeneficiario_TenantATriesToAccessTenantB_Returns404() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(get("/api/beneficiarios/" + BEN_MARIA_B_ID)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateBeneficiario_TenantATriesToUpdateTenantB_Returns404AndNoChanges() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);
        
        String updatePayload = "{\"matricula\":\"HACKED-999\",\"tipo\":\"TITULAR\",\"status\":\"ATIVO\",\"dataAdesao\":\"2024-01-01\"}";

        mockMvc.perform(put("/api/beneficiarios/" + BEN_MARIA_B_ID)
                .header("Authorization", "Bearer " + tokenA)
                .contentType("application/json")
                .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Verify the record in Tenant B remains untouched
        String tokenB = getAuthToken(TENANT_B_ID, "admin", TEST_PASSWORD);
        mockMvc.perform(get("/api/beneficiarios/" + BEN_MARIA_B_ID)
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("MAT-200")); // Original value
    }

    @Test
    void deleteBeneficiario_TenantATriesToDeleteTenantB_Returns404AndNotDeleted() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(delete("/api/beneficiarios/" + BEN_MARIA_B_ID)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Verify the record still exists in Tenant B
        String tokenB = getAuthToken(TENANT_B_ID, "admin", TEST_PASSWORD);
        mockMvc.perform(get("/api/beneficiarios/" + BEN_MARIA_B_ID)
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());
    }

    @Test
    void tenantId_InUrlQueryParam_IsIgnoredAndTenantContextIsUsed() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        // Attempting to bypass Tenant A and access Tenant B's data
        MvcResult result = mockMvc.perform(get("/api/beneficiarios?tenantId=" + TENANT_B_ID.toString())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        
        // It must NOT contain any data from Tenant B
        assertFalse(response.contains("MAT-200"));
        // It must STILL return Tenant A's data
        assertTrue(response.contains("MAT-100"));
        assertTrue(response.contains(TENANT_A_ID.toString()));
    }
}

package br.com.jonas.multitenant;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PessoaServiceIntegrationTest extends IntegrationTestBase {

    @Test
    void caso1_tentarExcluirPessoaComBeneficiario_Garante409() throws Exception {
        String token = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(delete("/api/pessoas/" + PESSOA_JOAO_ID)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível excluir Pessoa com Beneficiários vinculados"));
    }

    @Test
    void caso2_excluirBeneficiarioEDepoisPessoa_GaranteSucesso() throws Exception {
        String token = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(delete("/api/beneficiarios/" + BEN_JOAO_A_ID)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/pessoas/" + PESSOA_JOAO_ID)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void caso3_excluirBeneficiarioDeUmTenantMasPessoaTemOutroEmOutroTenant_Garante409() throws Exception {
        String tokenA = getAuthToken(TENANT_A_ID, "admin", TEST_PASSWORD);

        mockMvc.perform(delete("/api/beneficiarios/" + BEN_MARIA_A_ID)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/pessoas/" + PESSOA_MARIA_ID)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível excluir Pessoa com Beneficiários vinculados"));
    }
}

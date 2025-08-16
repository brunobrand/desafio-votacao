package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.domain.controller.PautaController;
import br.com.cooperativa.votacao_api.domain.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.service.PautaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; // Import necessário
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration; // Import necessário
import org.springframework.context.annotation.Bean; // Import necessário
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PautaController.class)
public class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaService pautaService;
    
    @TestConfiguration
    static class PautaControllerTestConfig {
        @Bean
        public PautaService pautaService() {
            return Mockito.mock(PautaService.class);
        }
    }

    @Test
    void deveCriarPauta_quandoEnviarDadosValidos_retornarStatus201() throws Exception {
        // Arrange
        PautaDTO pautaDTO = new PautaDTO("Pauta Teste", "Descrição Teste");
        Pauta pautaSalva = new Pauta();
        pautaSalva.setId(1L);
        pautaSalva.setTitulo(pautaDTO.titulo());
        pautaSalva.setDescricao(pautaDTO.descricao());

        when(pautaService.criarPauta(any(PautaDTO.class))).thenReturn(pautaSalva);

        // Act & Assert
        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pautaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"));
    }
}
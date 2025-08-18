package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.domain.controller.PautaController;
import br.com.cooperativa.votacao_api.domain.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.service.PautaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration; 
import org.springframework.context.annotation.Bean; 
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
       
        PautaDTO pautaDTO = new PautaDTO("Pauta Teste", "Descrição Teste");
        Pauta pautaSalva = new Pauta();
        pautaSalva.setId(1L);
        pautaSalva.setTitulo(pautaDTO.titulo());
        pautaSalva.setDescricao(pautaDTO.descricao());

        when(pautaService.criarPauta(any(PautaDTO.class))).thenReturn(pautaSalva);

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pautaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"));
    }

    @Test
    void deveRetornarStatusBadRequest_quandoTentarCriarPautaComTituloNulo() throws Exception {
        
        PautaDTO pautaInvalidaDTO = new PautaDTO(null, "Descrição sem título");
        String mensagemDeErroEsperada = "O título da pauta não pode ser nulo ou vazio.";

        when(pautaService.criarPauta(any(PautaDTO.class)))
            .thenThrow(new IllegalArgumentException(mensagemDeErroEsperada));

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pautaInvalidaDTO)))
                .andExpect(status().isBadRequest()) 
                .andExpect(content().string(mensagemDeErroEsperada));
    }
}
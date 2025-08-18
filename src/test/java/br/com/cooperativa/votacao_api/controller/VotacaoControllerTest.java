package br.com.cooperativa.votacao_api.controller;

import  br.com.cooperativa.votacao_api.domain.controller.dto.AbrirSessaoDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import br.com.cooperativa.votacao_api.domain.controller.VotacaoController;
import br.com.cooperativa.votacao_api.service.VotacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotacaoController.class)
class VotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VotacaoService votacaoService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public VotacaoService votacaoService() {
            return Mockito.mock(VotacaoService.class);
        }
    }

    @Test
    void deveAbrirSessao_eRetornarStatus201_quandoPautaExistir() throws Exception {
        long pautaId = 1L;
        var requestDTO = new AbrirSessaoDTO(10);

        Pauta pauta = new Pauta();
        pauta.setId(pautaId);
        SessaoVotacao sessaoCriada = new SessaoVotacao(pauta, LocalDateTime.now().plusMinutes(10));
        sessaoCriada.setId(1L);

        when(votacaoService.abrirSessao(eq(pautaId), any())).thenReturn(sessaoCriada);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId) 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Espera 201 Created
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pauta.id").value(pautaId));
    }
}
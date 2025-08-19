package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.controller.advice.RestExceptionHandler;
import br.com.cooperativa.votacao_api.controller.dto.VotoRequestDTO;
import br.com.cooperativa.votacao_api.service.VotacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
@Import(RestExceptionHandler.class)
class VotoControllerTest {

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
    void deveRegistrarVoto_eRetornarStatusOK_quandoDadosValidos() throws Exception {
        long sessaoId = 1L;
        var votoDTO = new VotoRequestDTO("12345678901", "Sim");

        doNothing().when(votacaoService).registrarVoto(any(Long.class), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{sessaoId}/votos", sessaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(votoDTO)))
                .andExpect(status().isOk()); 
    }

    @Test
    void deveRetornarStatusBadRequest_quandoTentarVotarEmSessaoEncerrada() throws Exception {
        long sessaoId = 2L;
        var votoDTO = new VotoRequestDTO("12345678901", "Sim");
        String mensagemDeErro = "A sessão de votação já está encerrada.";

        doThrow(new IllegalStateException(mensagemDeErro))
            .when(votacaoService).registrarVoto(any(Long.class), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{sessaoId}/votos", sessaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(votoDTO)))
                .andExpect(status().isBadRequest()); 
    }

    @Test
    void deveRetornarStatusBadRequest_quandoAssociadoTentarVotarDuasVezes() throws Exception {
        long sessaoId = 3L;
        var votoDTO = new VotoRequestDTO("11122233344", "Não");
        String mensagemDeErro = "Associado já votou nesta pauta.";

        doThrow(new IllegalStateException(mensagemDeErro))
            .when(votacaoService).registrarVoto(any(Long.class), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/v1/sessoes/{sessaoId}/votos", sessaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(votoDTO)))
                .andExpect(status().isBadRequest());
    }
}
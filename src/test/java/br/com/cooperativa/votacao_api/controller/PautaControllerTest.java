package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.controller.advice.RestExceptionHandler;
import br.com.cooperativa.votacao_api.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.controller.dto.ResultadoDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.service.PautaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(PautaController.class)
@Import(RestExceptionHandler.class)

public class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaService pautaService;

    @Autowired 
    private VotacaoService votacaoService;
    
    @TestConfiguration
    static class PautaControllerTestConfig {
        @Bean
        public PautaService pautaService() {
            return Mockito.mock(PautaService.class);
        }
        @Bean
        public VotacaoService votacaoService() {
            return Mockito.mock(VotacaoService.class);
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

    @Test
    void deveRetornarResultadoDaPauta_quandoSolicitado() throws Exception {

        long pautaId = 1L;
        var resultadoDTO = new ResultadoDTO(pautaId, 1L, 15L, 10L, "Aprovada");

        when(votacaoService.contabilizarResultado(pautaId)).thenReturn(resultadoDTO);

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId)) // GET
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.votosSim").value(15L))
                .andExpect(jsonPath("$.votosNao").value(10L))
                .andExpect(jsonPath("$.resultado").value("Aprovada"));
    }
}
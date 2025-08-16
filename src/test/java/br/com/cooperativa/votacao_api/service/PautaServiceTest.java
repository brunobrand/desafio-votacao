package br.com.cooperativa.votacao_api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.domain.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.domain.repository.PautaRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class PautaServiceTest {

    @Mock 
    private PautaRepository pautaRepository;

    @InjectMocks 
    private PautaService pautaService;

    @Test
    void deveCriarPautaComSucesso() {
        // Arrange 
        var pautaDto = new PautaDTO("Título da Pauta", "Descrição da Pauta");
        var pautaSalva = new Pauta();
        pautaSalva.setId(1L);
        pautaSalva.setTitulo("Título da Pauta");

        // Configurando o comportamento do mock
        when(pautaRepository.save(any(Pauta.class))).thenReturn(pautaSalva);

        // Act 
        Pauta resultado = pautaService.criarPauta(pautaDto);

        // Assert 
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Título da Pauta", resultado.getTitulo());
        verify(pautaRepository, times(1)).save(any(Pauta.class)); // Verifica se o save foi chamado 1 vez
    }
}
package br.com.cooperativa.votacao_api.service;

import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import br.com.cooperativa.votacao_api.domain.repository.PautaRepository;
import br.com.cooperativa.votacao_api.domain.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotacaoServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @InjectMocks
    private VotacaoService votacaoService;

    @Test
    void deveAbrirSessaoDeVotacaoComDuracaoEspecifica() {

        long pautaId = 1L;
        int duracaoEmMinutos = 10;
        Pauta pautaExistente = new Pauta();
        pautaExistente.setId(pautaId);

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pautaExistente));

        when(sessaoVotacaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoVotacao sessaoAberta = votacaoService.abrirSessao(pautaId, duracaoEmMinutos);

        ArgumentCaptor<SessaoVotacao> sessaoCaptor = ArgumentCaptor.forClass(SessaoVotacao.class);
        verify(sessaoVotacaoRepository).save(sessaoCaptor.capture());

        SessaoVotacao sessaoSalva = sessaoCaptor.getValue();

        assertNotNull(sessaoAberta);
        assertEquals(pautaId, sessaoSalva.getPauta().getId());
        assertTrue(sessaoSalva.getDataFechamento().isAfter(LocalDateTime.now().plusMinutes(9)));
        assertTrue(sessaoSalva.getDataFechamento().isBefore(LocalDateTime.now().plusMinutes(11)));
    }

    @Test
    void deveAbrirSessaoComDuracaoPadrao_quandoDuracaoNaoForInformada() {
        long pautaId = 2L;
        Pauta pautaExistente = new Pauta();
        pautaExistente.setId(pautaId);

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pautaExistente));

        votacaoService.abrirSessao(pautaId, null);

        ArgumentCaptor<SessaoVotacao> sessaoCaptor = ArgumentCaptor.forClass(SessaoVotacao.class);
        verify(sessaoVotacaoRepository).save(sessaoCaptor.capture());
        
        SessaoVotacao sessaoSalva = sessaoCaptor.getValue();

        long minutosDeDiferenca = java.time.Duration.between(sessaoSalva.getDataAbertura(), sessaoSalva.getDataFechamento()).toMinutes();
        assertEquals(1, minutosDeDiferenca);
    }


    @Test
    void deveLancarExcecao_aoTentarAbrirSessaoParaPautaInexistente() {
        long pautaIdInexistente = 99L;

        when(pautaRepository.findById(pautaIdInexistente)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            votacaoService.abrirSessao(pautaIdInexistente, 5);
        });

        verify(sessaoVotacaoRepository, never()).save(any());
    }

}
package br.com.cooperativa.votacao_api.service;

import br.com.cooperativa.votacao_api.controller.dto.VotoRequestDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import br.com.cooperativa.votacao_api.domain.model.Voto;
import br.com.cooperativa.votacao_api.domain.repository.VotoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotacaoServiceTest {

    @Mock
    private VotoRepository votoRepository;
    
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

    @Test
    void deveRegistrarVotoComSucesso_quandoSessaoEstiverAbertaEAssociadoNaoVotou() {
        long sessaoId = 1L;
        var votoDTO = new VotoRequestDTO("12345678901", "Sim");

        Pauta pauta = new Pauta();

        SessaoVotacao sessaoAberta = new SessaoVotacao(pauta, LocalDateTime.now().plusHours(1));

        when(sessaoVotacaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessaoAberta));
        when(votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoId, votoDTO.cpfAssociado()))
            .thenReturn(false); 

 
        votacaoService.registrarVoto(sessaoId, votoDTO);

        
        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).save(votoCaptor.capture()); 
        Voto votoSalvo = votoCaptor.getValue();

        assertEquals(votoDTO.cpfAssociado(), votoSalvo.getCpfAssociado());
        assertTrue(votoSalvo.isVotoSim()); 
    }

    @Test
    void deveLancarExcecao_aoTentarVotarEmSessaoEncerrada() {
        long sessaoId = 2L;
        var votoDTO = new VotoRequestDTO("12345678901", "Sim");

        Pauta pauta = new Pauta();
        // sessão que fechou há 1 minuto atrás
        SessaoVotacao sessaoEncerrada = new SessaoVotacao(pauta, LocalDateTime.now().minusMinutes(1));

        // devolve a sessão já encerrada
        when(sessaoVotacaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessaoEncerrada));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            votacaoService.registrarVoto(sessaoId, votoDTO);
        });

        assertEquals("A sessão de votação já está encerrada.", exception.getMessage());

        verify(votoRepository, never()).save(any(Voto.class));
    }

}
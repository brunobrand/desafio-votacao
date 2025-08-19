package br.com.cooperativa.votacao_api.service;

import br.com.cooperativa.votacao_api.controller.dto.ResultadoDTO;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
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

        SessaoVotacao sessaoEncerrada = new SessaoVotacao(pauta, LocalDateTime.now().minusMinutes(1));

        when(sessaoVotacaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessaoEncerrada));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            votacaoService.registrarVoto(sessaoId, votoDTO);
        });

        assertEquals("A sessão de votação já está encerrada.", exception.getMessage());

        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveLancarExcecao_aoTentarVotarDuasVezesNaMesmaSessao() {
        long sessaoId = 3L;
        var votoDTO = new VotoRequestDTO("11122233344", "Não");

        Pauta pauta = new Pauta();
        SessaoVotacao sessaoAberta = new SessaoVotacao(pauta, LocalDateTime.now().plusHours(1));

        when(sessaoVotacaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessaoAberta));

        when(votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoId, votoDTO.cpfAssociado()))
            .thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            votacaoService.registrarVoto(sessaoId, votoDTO);
        });

        assertEquals("Associado já votou nesta pauta.", exception.getMessage());
        verify(votoRepository, never()).save(any(Voto.class));
    }


    @Test
    void deveContabilizarResultadoEAprovado_quandoHouverMaisVotosSim() {
        long pautaId = 1L;
        long sessaoId = 1L;
        Pauta pauta = new Pauta();
        pauta.setId(pautaId);

        SessaoVotacao sessaoEncerrada = new SessaoVotacao(pauta, LocalDateTime.now().minusMinutes(1));
        sessaoEncerrada.setId(sessaoId);

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId)).thenReturn(Optional.of(sessaoEncerrada));

        when(votoRepository.countBySessaoVotacaoIdAndVotoSim(sessaoId, true)).thenReturn(10L); 
        when(votoRepository.countBySessaoVotacaoIdAndVotoSim(sessaoId, false)).thenReturn(5L);  

        ResultadoDTO resultado = votacaoService.contabilizarResultado(pautaId);

        assertNotNull(resultado);
        assertEquals(10L, resultado.votosSim());
        assertEquals(5L, resultado.votosNao());
        assertEquals("Aprovada", resultado.resultado());
    }


    @Test
    void deveContabilizarResultadoEReprovado_quandoHouverMaisVotosNao() {
        long pautaId = 2L;
        long sessaoId = 2L;
        Pauta pauta = new Pauta();
        pauta.setId(pautaId);

        SessaoVotacao sessaoEncerrada = new SessaoVotacao();
        sessaoEncerrada.setId(sessaoId);
        sessaoEncerrada.setPauta(pauta);
        sessaoEncerrada.setDataFechamento(LocalDateTime.now().minusMinutes(1)); 

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId)).thenReturn(Optional.of(sessaoEncerrada));

        when(votoRepository.countBySessaoVotacaoIdAndVotoSim(sessaoId, true)).thenReturn(5L);  
        when(votoRepository.countBySessaoVotacaoIdAndVotoSim(sessaoId, false)).thenReturn(10L); 

        ResultadoDTO resultado = votacaoService.contabilizarResultado(pautaId);

        assertNotNull(resultado);
        assertEquals(5L, resultado.votosSim());
        assertEquals(10L, resultado.votosNao());
        assertEquals("Reprovada", resultado.resultado());
    }
    

    @Test
    void deveContabilizarResultadoEEmpate_quandoHouverNumeroIgualDeVotos() {
        long pautaId = 3L;
        long sessaoId = 3L;
        Pauta pauta = new Pauta();
        pauta.setId(pautaId);
        
        SessaoVotacao sessaoEncerrada = new SessaoVotacao();
        sessaoEncerrada.setId(sessaoId);
        sessaoEncerrada.setPauta(pauta);
        sessaoEncerrada.setDataFechamento(LocalDateTime.now().minusMinutes(1));

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId)).thenReturn(Optional.of(sessaoEncerrada));
        
        when(votoRepository.countBySessaoVotacaoIdAndVotoSim(sessaoId, true)).thenReturn(7L); // 7 votos Sim
        when(votoRepository.countBySessaoVotacaoIdAndVotoSim(sessaoId, false)).thenReturn(7L); // 7 votos Não

        ResultadoDTO resultado = votacaoService.contabilizarResultado(pautaId);

        assertNotNull(resultado);
        assertEquals(7L, resultado.votosSim());
        assertEquals(7L, resultado.votosNao());
        assertEquals("Empate", resultado.resultado());
    }

    
    @Test
    void deveRetornarMensagemDeSessaoAberta_quandoContabilizarResultadoDeSessaoNaoEncerrada() {
        long pautaId = 4L;
        long sessaoId = 4L;
        Pauta pauta = new Pauta();
        pauta.setId(pautaId);

        SessaoVotacao sessaoAberta = new SessaoVotacao();
        sessaoAberta.setId(sessaoId);
        sessaoAberta.setPauta(pauta);
        sessaoAberta.setDataFechamento(LocalDateTime.now().plusHours(1));

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId)).thenReturn(Optional.of(sessaoAberta));

        ResultadoDTO resultado = votacaoService.contabilizarResultado(pautaId);

        assertNotNull(resultado);
        assertEquals("A sessão de votação ainda está aberta.", resultado.resultado());
        
        verify(votoRepository, never()).countBySessaoVotacaoIdAndVotoSim(anyLong(), anyBoolean());
    }
}
package br.com.cooperativa.votacao_api.service;

import br.com.cooperativa.votacao_api.controller.dto.VotoRequestDTO;
import br.com.cooperativa.votacao_api.controller.dto.ResultadoDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import br.com.cooperativa.votacao_api.domain.model.Voto;
import br.com.cooperativa.votacao_api.domain.repository.PautaRepository;
import br.com.cooperativa.votacao_api.domain.repository.SessaoVotacaoRepository;
import br.com.cooperativa.votacao_api.domain.repository.VotoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class VotacaoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final VotoRepository votoRepository; 

    public SessaoVotacao abrirSessao(Long pautaId, Integer duracaoEmMinutos) {
    var pauta = pautaRepository.findById(pautaId)
            .orElseThrow(() -> new EntityNotFoundException("Pauta com ID " + pautaId + " não encontrada."));

    Integer duracaoFinal = (duracaoEmMinutos == null || duracaoEmMinutos <= 0) ? 1 : duracaoEmMinutos;

    LocalDateTime dataAbertura = LocalDateTime.now();
    LocalDateTime dataFechamento = dataAbertura.plusMinutes(duracaoFinal);

    SessaoVotacao novaSessao = new SessaoVotacao();
    novaSessao.setPauta(pauta);
    novaSessao.setDataAbertura(dataAbertura);
    novaSessao.setDataFechamento(dataFechamento);

    return sessaoVotacaoRepository.save(novaSessao);
}

    public void registrarVoto(Long sessaoId, VotoRequestDTO votoDTO) {
        if (!"Sim".equalsIgnoreCase(votoDTO.voto()) && !"Não".equalsIgnoreCase(votoDTO.voto())) {
            throw new IllegalArgumentException("O voto deve ser 'Sim' ou 'Não'.");
        }

        SessaoVotacao sessao = sessaoVotacaoRepository.findById(sessaoId)
                .orElseThrow(() -> new EntityNotFoundException("Sessão com ID " + sessaoId + " não encontrada."));

        if (LocalDateTime.now().isAfter(sessao.getDataFechamento())) {
            throw new IllegalStateException("A sessão de votação já está encerrada.");
        }

        boolean jaVotou = votoRepository.existsBySessaoVotacaoIdAndCpfAssociado(sessaoId, votoDTO.cpfAssociado());
        if (jaVotou) {
            throw new IllegalStateException("Associado já votou nesta pauta.");
        }

        Voto novoVoto = new Voto();
        novoVoto.setSessaoVotacao(sessao);
        novoVoto.setCpfAssociado(votoDTO.cpfAssociado());
        novoVoto.setVotoSim("Sim".equalsIgnoreCase(votoDTO.voto()));

        votoRepository.save(novoVoto);
    }

    public ResultadoDTO contabilizarResultado(Long pautaId) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new EntityNotFoundException("Pauta com ID " + pautaId + " não encontrada."));

        Optional<SessaoVotacao> sessaoOpt = sessaoVotacaoRepository.findFirstByPautaIdOrderByIdDesc(pautaId);

        if (sessaoOpt.isEmpty()) {
            return new ResultadoDTO(pautaId, null, 0L, 0L, "Nenhuma sessão de votação encontrada para esta pauta.");
        }

        SessaoVotacao sessao = sessaoOpt.get();

        if (LocalDateTime.now().isBefore(sessao.getDataFechamento())) {
            return new ResultadoDTO(pautaId, sessao.getId(), 0L, 0L, "A sessão de votação ainda está aberta.");
        }

        Long votosSim = votoRepository.countBySessaoVotacaoIdAndVotoSim(sessao.getId(), true);
        Long votosNao = votoRepository.countBySessaoVotacaoIdAndVotoSim(sessao.getId(), false);

        String resultadoFinal;
        if (votosSim > votosNao) {
            resultadoFinal = "Aprovada";
        } else if (votosNao > votosSim) {
            resultadoFinal = "Reprovada";
        } else {
            resultadoFinal = "Empate";
        }

        return new ResultadoDTO(pautaId, sessao.getId(), votosSim, votosNao, resultadoFinal);
    }
}
package br.com.cooperativa.votacao_api.service;

import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import br.com.cooperativa.votacao_api.domain.repository.PautaRepository;
import br.com.cooperativa.votacao_api.domain.repository.SessaoVotacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class VotacaoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;

    public SessaoVotacao abrirSessao(Long pautaId, Integer duracaoEmMinutos) {
       var pauta = pautaRepository.findById(pautaId)
            .orElseThrow(() -> new EntityNotFoundException("Pauta com ID " + pautaId + " não encontrada."));

        Integer duracaoFinal = (duracaoEmMinutos == null || duracaoEmMinutos <= 0) ? 1 : duracaoEmMinutos;

        LocalDateTime dataFechamento = LocalDateTime.now().plusMinutes(duracaoFinal);
        
        SessaoVotacao novaSessao = new SessaoVotacao(pauta, dataFechamento);

        return sessaoVotacaoRepository.save(novaSessao);
    }
    
}
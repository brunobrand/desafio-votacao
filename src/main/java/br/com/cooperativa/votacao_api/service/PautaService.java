package br.com.cooperativa.votacao_api.service;

import br.com.cooperativa.votacao_api.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.domain.repository.PautaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor 
public class PautaService {
    
    private final PautaRepository pautaRepository;

    public Pauta criarPauta(PautaDTO dto) {
        
        if (dto.titulo() == null || dto.titulo().isBlank()) {
            throw new IllegalArgumentException("O título da pauta não pode ser nulo ou vazio.");
        }

        Pauta pauta = new Pauta();
        pauta.setTitulo(dto.titulo());
        pauta.setDescricao(dto.descricao());
        
        return pautaRepository.save(pauta);
    }
}

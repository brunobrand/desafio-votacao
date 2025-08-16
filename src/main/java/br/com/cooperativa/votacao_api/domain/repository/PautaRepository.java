package br.com.cooperativa.votacao_api.domain.repository;

import br.com.cooperativa.votacao_api.domain.model.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long> {
    
}
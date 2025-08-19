package br.com.cooperativa.votacao_api.domain.repository;

import java.util.Optional;
import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {
    // Encontra a primeira (findFirst) por PautaId, ordenando por ID de forma decrescente (OrderByIdDesc)
    Optional<SessaoVotacao> findFirstByPautaIdOrderByIdDesc(Long pautaId);
}
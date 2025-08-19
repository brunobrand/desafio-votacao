package br.com.cooperativa.votacao_api.domain.repository;

import br.com.cooperativa.votacao_api.domain.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {
    boolean existsBySessaoVotacaoIdAndCpfAssociado(Long sessaoVotacaoId, String cpfAssociado);

    Long countBySessaoVotacaoIdAndVotoSim(Long sessaoVotacaoId, boolean votoSim);
}
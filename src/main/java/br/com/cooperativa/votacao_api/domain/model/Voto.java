package br.com.cooperativa.votacao_api.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "votos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sessao_votacao_id", "cpf_associado"})
})
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sessao_votacao_id", nullable = false)
    private SessaoVotacao sessaoVotacao;

    @Column(name = "cpf_associado", nullable = false)
    private String cpfAssociado;

    @Column(name = "voto_sim", nullable = false)
    private boolean votoSim; 
}
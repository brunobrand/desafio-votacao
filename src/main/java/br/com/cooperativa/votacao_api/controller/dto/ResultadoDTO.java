package br.com.cooperativa.votacao_api.controller.dto;

public record ResultadoDTO(
        Long pautaId,
        Long sessaoId,
        Long votosSim,
        Long votosNao,
        String resultado
) {
}
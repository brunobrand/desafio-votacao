package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.controller.dto.AbrirSessaoDTO;
import br.com.cooperativa.votacao_api.domain.model.SessaoVotacao;
import br.com.cooperativa.votacao_api.service.VotacaoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/sessoes")
@AllArgsConstructor
public class VotacaoController {

    private final VotacaoService votacaoService;

    @PostMapping
    public ResponseEntity<SessaoVotacao> abrirSessao(@PathVariable Long pautaId, @RequestBody(required = false) AbrirSessaoDTO dto) {
        Integer duracao = (dto != null) ? dto.duracaoEmMinutos() : null;
        SessaoVotacao sessaoAberta = votacaoService.abrirSessao(pautaId, duracao);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(sessaoAberta.getId())
                .toUri();

        return ResponseEntity.created(location).body(sessaoAberta);
    }
}
package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.controller.dto.VotoRequestDTO;
import br.com.cooperativa.votacao_api.service.VotacaoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessoes/{sessaoId}/votos")
@AllArgsConstructor
public class VotoController {

    private final VotacaoService votacaoService;

    @PostMapping
    public ResponseEntity<Void> registrarVoto(@PathVariable Long sessaoId, @RequestBody VotoRequestDTO votoDTO) {
        votacaoService.registrarVoto(sessaoId, votoDTO);
        return ResponseEntity.ok().build();
    }
}
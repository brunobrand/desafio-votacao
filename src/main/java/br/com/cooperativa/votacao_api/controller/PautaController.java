package br.com.cooperativa.votacao_api.controller;

import br.com.cooperativa.votacao_api.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.controller.dto.ResultadoDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.service.PautaService;
import br.com.cooperativa.votacao_api.service.VotacaoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
@AllArgsConstructor
public class PautaController {

    private final PautaService pautaService;
    private final VotacaoService votacaoService;

    @PostMapping
    public ResponseEntity<Pauta> criarPauta(@RequestBody PautaDTO pautaDTO) {
        Pauta pautaSalva = pautaService.criarPauta(pautaDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pautaSalva.getId())
                .toUri();

        return ResponseEntity.created(location).body(pautaSalva);
    }

     @GetMapping("/{pautaId}/resultado")
    public ResponseEntity<ResultadoDTO> obterResultado(@PathVariable Long pautaId) {
        ResultadoDTO resultadoDTO = votacaoService.contabilizarResultado(pautaId);
        return ResponseEntity.ok(resultadoDTO);
    }
}
package br.com.cooperativa.votacao_api.domain.controller;

import br.com.cooperativa.votacao_api.domain.controller.dto.PautaDTO;
import br.com.cooperativa.votacao_api.domain.model.Pauta;
import br.com.cooperativa.votacao_api.service.PautaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Pauta> criarPauta(@RequestBody PautaDTO pautaDTO) {
        Pauta pautaSalva = pautaService.criarPauta(pautaDTO);

        // Cria a URI do novo recurso criado para retornar no header "Location"
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pautaSalva.getId())
                .toUri();

        return ResponseEntity.created(location).body(pautaSalva);
    }
}
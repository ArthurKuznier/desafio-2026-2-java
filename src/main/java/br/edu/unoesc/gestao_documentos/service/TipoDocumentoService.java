package br.edu.unoesc.gestao_documentos.service;

import org.springframework.stereotype.Service;

import br.edu.unoesc.gestao_documentos.repositories.TipoDocumentoRepository;
import br.edu.unoesc.gestao_documentos.domain.TipoDocumento;

@Service
public class TipoDocumentoService {
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public TipoDocumento salvar(TipoDocumento tipoDocumento) {
        return this.tipoDocumentoRepository.save(tipoDocumento);
    }
}

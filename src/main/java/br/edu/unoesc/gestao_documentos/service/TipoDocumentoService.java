package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.TipoDocumento;
import br.edu.unoesc.gestao_documentos.repositories.TipoDocumentoRepository;
import org.springframework.stereotype.Service;

@Service
public class TipoDocumentoService extends AbstractCrudService<TipoDocumento, Integer> {

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        super(tipoDocumentoRepository);
    }
}

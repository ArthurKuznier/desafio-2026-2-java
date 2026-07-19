package br.edu.unoesc.gestao_documentos.config;

import br.edu.unoesc.gestao_documentos.domain.Role;
import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.domain.StatusNome;
import br.edu.unoesc.gestao_documentos.domain.Usuario;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import br.edu.unoesc.gestao_documentos.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private record StatusSeed(StatusNome nome, Integer responsavel, boolean finalizaSolicitacao) {
    }

    private static final List<StatusSeed> STATUS_PADRAO = List.of(
            new StatusSeed(StatusNome.ABERTA, 1, false),
            new StatusSeed(StatusNome.EM_ANALISE, 2, false),
            new StatusSeed(StatusNome.APROVADA, 3, false),
            new StatusSeed(StatusNome.EMITIDA, 4, true),
            new StatusSeed(StatusNome.REPROVADA, 2, true));

    private final UsuarioRepository usuarioRepository;
    private final StatusRepository statusRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    public DatabaseSeeder(UsuarioRepository usuarioRepository, StatusRepository statusRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.statusRepository = statusRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        semearAdmin();
        semearStatusPadrao();
    }

    private void semearAdmin() {
        if (usuarioRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setUsername(adminUsername);
        admin.setSenha(passwordEncoder.encode(adminPassword));
        admin.setNome("Administrador Sistema");
        admin.setRole(Role.ADMIN);
        admin.setAtivo(true);

        usuarioRepository.save(admin);
        log.info("Usuário administrador semeado com sucesso");
    }

    private void semearStatusPadrao() {
        for (StatusSeed seed : STATUS_PADRAO) {
            if (statusRepository.findByNomeIgnoreCase(seed.nome().name()).isPresent()) {
                continue;
            }
            Status status = new Status();
            status.setNome(seed.nome().name());
            status.setResponsavel(seed.responsavel());
            status.setFinalizaSolicitacao(seed.finalizaSolicitacao());
            statusRepository.save(status);
            log.info("Status '{}' semeado com sucesso", seed.nome());
        }
    }
}
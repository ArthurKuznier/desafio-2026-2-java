package br.edu.unoesc.gestao_documentos.config;

import br.edu.unoesc.gestao_documentos.domain.Role;
import br.edu.unoesc.gestao_documentos.domain.Usuario;
import br.edu.unoesc.gestao_documentos.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    public DatabaseSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
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
}

package br.edu.unoesc.gestao_documentos.config;

import br.edu.unoesc.gestao_documentos.domain.Role;
import br.edu.unoesc.gestao_documentos.domain.Usuario;
import br.edu.unoesc.gestao_documentos.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

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
    public void run(String... args) throws Exception {
        if (usuarioRepository.findByUsername(adminUsername) == null) {
            Usuario admin = new Usuario();
            admin.setUsername(adminUsername);
            admin.setSenha(passwordEncoder.encode(adminPassword));
            admin.setNome("Administrador Sistema");
            admin.setRole(Role.ADMIN);
            admin.setAtivo(true);

            usuarioRepository.save(admin);
            System.out.println(">>> [SEED] Usuário administrador semeado com sucesso!");
        }
    }
}
package br.edu.unoesc.gestao_documentos.repositories;

import br.edu.unoesc.gestao_documentos.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    UserDetails findByUsername(String username);
}
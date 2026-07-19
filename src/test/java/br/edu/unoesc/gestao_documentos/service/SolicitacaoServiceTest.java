package br.edu.unoesc.gestao_documentos.service;

import br.edu.unoesc.gestao_documentos.domain.Aluno;
import br.edu.unoesc.gestao_documentos.domain.Prioridade;
import br.edu.unoesc.gestao_documentos.domain.Role;
import br.edu.unoesc.gestao_documentos.domain.Solicitacao;
import br.edu.unoesc.gestao_documentos.domain.Status;
import br.edu.unoesc.gestao_documentos.domain.Usuario;
import br.edu.unoesc.gestao_documentos.exception.RegraNegocioException;
import br.edu.unoesc.gestao_documentos.repositories.AlunoRepository;
import br.edu.unoesc.gestao_documentos.repositories.SolicitacaoRepository;
import br.edu.unoesc.gestao_documentos.repositories.StatusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;
    @Mock
    private StatusRepository statusRepository;
    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Usuario criarUsuarioAdmin() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setRole(Role.ADMIN);
        usuario.setAtivo(true);
        return usuario;
    }

    private Usuario criarUsuarioResponsavel(Integer codigoResponsavel) {
        Usuario usuario = new Usuario();
        usuario.setUsername("secretaria");
        usuario.setRole(Role.RESPONSAVEL);
        usuario.setCodigoResponsavel(codigoResponsavel);
        usuario.setAtivo(true);
        return usuario;
    }

    private Status criarStatus(Integer id, String nome, Integer responsavel) {
        Status status = new Status();
        status.setId(id);
        status.setNome(nome);
        status.setResponsavel(responsavel);
        status.setFinalizaSolicitacao(false);
        return status;
    }

    private Solicitacao criarSolicitacaoComStatus(Status status) {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(1);
        solicitacao.setStatus(status);
        solicitacao.setPrioridade(Prioridade.NORMAL);
        return solicitacao;
    }

    @Test
    void deveTransicionarDeAbertaParaEmAnaliseQuandoValido() {
        Status aberta = criarStatus(1, "ABERTA", 1);
        Status emAnalise = criarStatus(2, "EM_ANALISE", 2);
        Solicitacao solicitacao = criarSolicitacaoComStatus(aberta);

        when(solicitacaoRepository.buscarCompletoPorId(1)).thenReturn(Optional.of(solicitacao));
        when(statusRepository.findById(2)).thenReturn(Optional.of(emAnalise));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        autenticarComo(criarUsuarioResponsavel(2));

        Solicitacao resultado = solicitacaoService.alterarStatus(1, 2);

        assertThat(resultado.getStatus().getNome()).isEqualTo("EM_ANALISE");
    }

    @Test
    void naoDevePermitirTransicaoInvalida() {
        Status aberta = criarStatus(1, "ABERTA", 1);
        Status emitida = criarStatus(4, "EMITIDA", 4);
        Solicitacao solicitacao = criarSolicitacaoComStatus(aberta);

        when(solicitacaoRepository.buscarCompletoPorId(1)).thenReturn(Optional.of(solicitacao));
        when(statusRepository.findById(4)).thenReturn(Optional.of(emitida));

        autenticarComo(criarUsuarioAdmin());

        assertThatThrownBy(() -> solicitacaoService.alterarStatus(1, 4))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Transição inválida");
    }

    @Test
    void adminPodeAlterarQualquerEtapaMesmoSemCodigoResponsavel() {
        Status aberta = criarStatus(1, "ABERTA", 1);
        Status emAnalise = criarStatus(2, "EM_ANALISE", 2);
        Solicitacao solicitacao = criarSolicitacaoComStatus(aberta);

        when(solicitacaoRepository.buscarCompletoPorId(1)).thenReturn(Optional.of(solicitacao));
        when(statusRepository.findById(2)).thenReturn(Optional.of(emAnalise));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        autenticarComo(criarUsuarioAdmin()); // sem codigoResponsavel definido

        Solicitacao resultado = solicitacaoService.alterarStatus(1, 2);

        assertThat(resultado.getStatus().getNome()).isEqualTo("EM_ANALISE");
    }

    @Test
    void responsavelComCodigoErradoNaoPodeAlterarStatus() {
        Status aberta = criarStatus(1, "ABERTA", 1);
        Status emAnalise = criarStatus(2, "EM_ANALISE", 2);
        Solicitacao solicitacao = criarSolicitacaoComStatus(aberta);

        when(solicitacaoRepository.buscarCompletoPorId(1)).thenReturn(Optional.of(solicitacao));
        when(statusRepository.findById(2)).thenReturn(Optional.of(emAnalise));

        autenticarComo(criarUsuarioResponsavel(99)); // codigo errado, EM_ANALISE pede 2

        assertThatThrownBy(() -> solicitacaoService.alterarStatus(1, 2))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é responsável");
    }

    @Test
    void deveLancarExcecaoQuandoSolicitacaoNaoExiste() {
        when(solicitacaoRepository.buscarCompletoPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitacaoService.alterarStatus(99, 1))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveImpedirCriarSolicitacaoParaAlunoInativo() {
        Aluno alunoInativo = new Aluno();
        alunoInativo.setId(5);
        alunoInativo.setNome("Fulano de Tal");
        alunoInativo.setAtivo(false);

        Solicitacao solicitacao = new Solicitacao();
        Aluno referenciaAluno = new Aluno();
        referenciaAluno.setId(5);
        solicitacao.setAluno(referenciaAluno);
        solicitacao.setPrioridade(Prioridade.NORMAL);

        when(alunoRepository.findById(5)).thenReturn(Optional.of(alunoInativo));

        assertThatThrownBy(() -> solicitacaoService.criarSolicitacao(solicitacao))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }
}
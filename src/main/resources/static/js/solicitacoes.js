const TOKEN_KEY = 'gestaoDocumentos.jwt';
const USERNAME_KEY = 'gestaoDocumentos.username';

const TRANSICOES_VALIDAS = {
    ABERTA: ['EM_ANALISE'],
    EM_ANALISE: ['APROVADA', 'REPROVADA'],
    APROVADA: ['EMITIDA'],
    EMITIDA: [],
    REPROVADA: []
};

let statusPorNome = {};

document.addEventListener('DOMContentLoaded', () => {
    const tokenSalvo = localStorage.getItem(TOKEN_KEY);
    if (tokenSalvo) {
        mostrarPainel();
        iniciar();
    } else {
        mostrarLogin();
    }

    document.getElementById('formLogin').addEventListener('submit', aoSubmeterLogin);
    document.getElementById('btnSair').addEventListener('click', logout);
    document.getElementById('formNovaSolicitacao').addEventListener('submit', aoCriarSolicitacao);
});

async function aoSubmeterLogin(evento) {
    evento.preventDefault();
    const username = document.getElementById('username').value.trim();
    const senha = document.getElementById('senha').value;
    const erroEl = document.getElementById('loginErro');
    erroEl.textContent = '';

    try {
        const resposta = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, senha })
        });

        if (!resposta.ok) {
            const mensagem = (resposta.status === 401 || resposta.status === 403)
                ? 'Usuario ou senha invalidos'
                : `Erro ao entrar (HTTP ${resposta.status})`;
            throw new Error(mensagem);
        }

        const dados = await resposta.json();
        localStorage.setItem(TOKEN_KEY, dados.token);
        localStorage.setItem(USERNAME_KEY, username);

        mostrarPainel();
        await iniciar();
    } catch (erro) {
        erroEl.textContent = erro.message;
    }
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    mostrarLogin();
}

function mostrarLogin() {
    document.getElementById('loginSection').classList.remove('escondido');
    document.getElementById('painelSection').classList.add('escondido');
    document.getElementById('senha').value = '';
}

function mostrarPainel() {
    document.getElementById('loginSection').classList.add('escondido');
    document.getElementById('painelSection').classList.remove('escondido');
    const username = localStorage.getItem(USERNAME_KEY);
    document.getElementById('usuarioLogado').textContent = username ? `Ola, ${username}` : '';
}

function obterToken() {
    return localStorage.getItem(TOKEN_KEY);
}

async function chamarApi(caminho, opcoes = {}) {
    const resposta = await fetch(caminho, {
        ...opcoes,
        headers: {
            Authorization: `Bearer ${obterToken()}`,
            ...(opcoes.body ? { 'Content-Type': 'application/json' } : {}),
            ...opcoes.headers
        }
    });

    if (resposta.status === 401 || resposta.status === 403) {
        logout();
        throw new Error('Sessao expirada, faca login novamente');
    }

    if (!resposta.ok) {
        let mensagem = `HTTP ${resposta.status}`;
        try {
            const corpo = await resposta.json();
            mensagem = corpo.mensagem || mensagem;
        } catch (ignorado) {
        }
        throw new Error(mensagem);
    }

    if (resposta.status === 204) {
        return null;
    }
    return resposta.json();
}

async function iniciar() {
    document.getElementById('mensagemErro').textContent = '';
    try {
        await Promise.all([
            carregarAlunos(),
            carregarCursos(),
            carregarTipos(),
            carregarStatus()
        ]);
        await carregarSolicitacoes();
    } catch (erro) {
        document.getElementById('mensagemErro').textContent = 'Falha ao carregar a tela: ' + erro.message;
    }
}

async function carregarAlunos() {
    const pagina = await chamarApi('/api/alunos?size=100');
    const select = document.getElementById('selectAluno');
    select.innerHTML = '';
    pagina.content
        .filter(aluno => aluno.ativo)
        .forEach(aluno => {
            const opcao = document.createElement('option');
            opcao.value = aluno.id;
            opcao.textContent = aluno.nome;
            select.appendChild(opcao);
        });
}

async function carregarCursos() {
    const pagina = await chamarApi('/api/cursos?size=100');
    const select = document.getElementById('selectCurso');
    select.innerHTML = '';
    pagina.content.forEach(curso => {
        const opcao = document.createElement('option');
        opcao.value = curso.id;
        opcao.textContent = curso.nome;
        select.appendChild(opcao);
    });
}

async function carregarTipos() {
    const pagina = await chamarApi('/api/tipoDocumento?size=100');
    const select = document.getElementById('selectTipo');
    select.innerHTML = '';
    pagina.content.forEach(tipo => {
        const opcao = document.createElement('option');
        opcao.value = tipo.id;
        opcao.textContent = tipo.nome;
        select.appendChild(opcao);
    });
}

async function carregarStatus() {
    const pagina = await chamarApi('/api/status?size=100');
    statusPorNome = {};
    pagina.content.forEach(status => {
        statusPorNome[status.nome] = status;
    });
}

function formatarUltimaAtualizacao(solicitacao) {
    const quem = solicitacao.ultimaAtualizacaoPor || 'sistema';
    const quando = solicitacao.dataAlteracao
        ? new Date(solicitacao.dataAlteracao).toLocaleString('pt-BR', {
            day: '2-digit', month: '2-digit', year: '2-digit',
            hour: '2-digit', minute: '2-digit'
        })
        : '--';
    return `<div class="ultima-atualizacao"><strong>${quem}</strong><span>${quando}</span></div>`;
}

async function carregarSolicitacoes() {
    const pagina = await chamarApi('/api/solicitacoes?size=50&sort=id,desc');
    const corpo = document.querySelector('#tabelaSolicitacoes tbody');
    corpo.innerHTML = '';

    pagina.content.forEach(solicitacao => {
        const linha = document.createElement('tr');

        const nomeStatus = solicitacao.status.nome;
        const proximosStatus = TRANSICOES_VALIDAS[nomeStatus] || [];

        let celulaAcao;
        if (proximosStatus.length === 0) {
            celulaAcao = '<span class="tag-status-final">Fluxo encerrado</span>';
        } else {
            const opcoesHtml = proximosStatus
                .map(nome => `<option value="${nome}">${nome}</option>`)
                .join('');
            celulaAcao = `
                <div class="acao-status">
                    <select class="select-novo-status">${opcoesHtml}</select>
                    <button type="button" class="btn-atualizar-status" data-id="${solicitacao.id}">Atualizar</button>
                </div>`;
        }

        linha.innerHTML = `
            <td>${solicitacao.id}</td>
            <td>${solicitacao.aluno.nome}</td>
            <td>${solicitacao.curso.nome}</td>
            <td>${solicitacao.tipo.nome}</td>
            <td>${solicitacao.prioridade}</td>
            <td><span class="tag-status ${nomeStatus}">${nomeStatus}</span></td>
            <td>${formatarUltimaAtualizacao(solicitacao)}</td>
            <td>${celulaAcao}</td>`;

        corpo.appendChild(linha);

        const botao = linha.querySelector('.btn-atualizar-status');
        if (botao) {
            botao.addEventListener('click', () => aoAtualizarStatus(linha, solicitacao.id));
        }
    });
}

async function aoAtualizarStatus(linha, solicitacaoId) {
    const nomeNovoStatus = linha.querySelector('.select-novo-status').value;
    const erroEl = document.getElementById('mensagemErro');
    erroEl.textContent = '';

    const status = statusPorNome[nomeNovoStatus];
    if (!status) {
        erroEl.textContent = 'Status ainda nao carregado, tenta recarregar a pagina.';
        return;
    }

    try {
        await chamarApi(`/api/solicitacoes/${solicitacaoId}`, {
            method: 'PATCH',
            body: JSON.stringify({ statusId: status.id })
        });
        await carregarSolicitacoes();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel atualizar o status: ' + erro.message;
    }
}

async function aoCriarSolicitacao(evento) {
    evento.preventDefault();
    const erroEl = document.getElementById('criarErro');
    const sucessoEl = document.getElementById('criarSucesso');
    erroEl.textContent = '';
    sucessoEl.textContent = '';

    const alunoId = document.getElementById('selectAluno').value;
    const cursoId = document.getElementById('selectCurso').value;
    const tipoId = document.getElementById('selectTipo').value;
    const prioridade = document.getElementById('selectPrioridade').value;

    try {
        await chamarApi('/api/solicitacoes', {
            method: 'POST',
            body: JSON.stringify({
                aluno: { id: Number(alunoId) },
                curso: { id: Number(cursoId) },
                tipo: { id: Number(tipoId) },
                prioridade
            })
        });
        sucessoEl.textContent = 'Solicitacao criada com sucesso!';
        await carregarSolicitacoes();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel criar a solicitacao: ' + erro.message;
    }
}
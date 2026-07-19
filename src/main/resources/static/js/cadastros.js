const TOKEN_KEY = 'gestaoDocumentos.jwt';
const USERNAME_KEY = 'gestaoDocumentos.username';

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
    document.getElementById('formAluno').addEventListener('submit', aoCriarAluno);
    document.getElementById('formCurso').addEventListener('submit', aoCriarCurso);
    document.getElementById('formTipo').addEventListener('submit', aoCriarTipo);
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
            // corpo nao veio em JSON, mantem a mensagem generica
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
            carregarTipos()
        ]);
    } catch (erro) {
        document.getElementById('mensagemErro').textContent = 'Falha ao carregar a tela: ' + erro.message;
    }
}

// ---------- Alunos ----------

async function carregarAlunos() {
    const pagina = await chamarApi('/api/alunos?size=100');
    const corpo = document.querySelector('#tabelaAlunos tbody');
    corpo.innerHTML = '';

    pagina.content.forEach(aluno => {
        const linha = document.createElement('tr');
        const ativo = aluno.ativo;
        linha.innerHTML = `
            <td>${aluno.nome}</td>
            <td><span class="tag-ativo ${ativo ? 'sim' : 'nao'}">${ativo ? 'Ativo' : 'Inativo'}</span></td>
            <td>
                <button type="button" class="btn-toggle-ativo">${ativo ? 'Desativar' : 'Ativar'}</button>
                <button type="button" class="btn-remover">Remover</button>
            </td>`;
        corpo.appendChild(linha);

        linha.querySelector('.btn-toggle-ativo').addEventListener('click', () => aoAlternarAtivo(aluno));
        linha.querySelector('.btn-remover').addEventListener('click', () => aoRemover('alunos', aluno.id, 'alunoErro', carregarAlunos));
    });
}

async function aoCriarAluno(evento) {
    evento.preventDefault();
    const erroEl = document.getElementById('alunoErro');
    erroEl.textContent = '';
    const nome = document.getElementById('nomeAluno').value.trim();

    try {
        await chamarApi('/api/alunos', {
            method: 'POST',
            body: JSON.stringify({ nome, ativo: true })
        });
        document.getElementById('nomeAluno').value = '';
        await carregarAlunos();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel criar: ' + erro.message;
    }
}

async function aoAlternarAtivo(aluno) {
    const erroEl = document.getElementById('alunoErro');
    erroEl.textContent = '';
    try {
        await chamarApi(`/api/alunos/${aluno.id}`, {
            method: 'PUT',
            body: JSON.stringify({ nome: aluno.nome, ativo: !aluno.ativo })
        });
        await carregarAlunos();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel atualizar: ' + erro.message;
    }
}

// ---------- Cursos ----------

async function carregarCursos() {
    const pagina = await chamarApi('/api/cursos?size=100');
    const corpo = document.querySelector('#tabelaCursos tbody');
    corpo.innerHTML = '';

    pagina.content.forEach(curso => {
        const linha = document.createElement('tr');
        linha.innerHTML = `
            <td>${curso.nome}</td>
            <td><button type="button" class="btn-remover">Remover</button></td>`;
        corpo.appendChild(linha);

        linha.querySelector('.btn-remover').addEventListener('click', () => aoRemover('cursos', curso.id, 'cursoErro', carregarCursos));
    });
}

async function aoCriarCurso(evento) {
    evento.preventDefault();
    const erroEl = document.getElementById('cursoErro');
    erroEl.textContent = '';
    const nome = document.getElementById('nomeCurso').value.trim();

    try {
        await chamarApi('/api/cursos', {
            method: 'POST',
            body: JSON.stringify({ nome })
        });
        document.getElementById('nomeCurso').value = '';
        await carregarCursos();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel criar: ' + erro.message;
    }
}

// ---------- Tipos de documento ----------

async function carregarTipos() {
    const pagina = await chamarApi('/api/tipoDocumento?size=100');
    const corpo = document.querySelector('#tabelaTipos tbody');
    corpo.innerHTML = '';

    pagina.content.forEach(tipo => {
        const linha = document.createElement('tr');
        linha.innerHTML = `
            <td>${tipo.nome}</td>
            <td><button type="button" class="btn-remover">Remover</button></td>`;
        corpo.appendChild(linha);

        linha.querySelector('.btn-remover').addEventListener('click', () => aoRemover('tipoDocumento', tipo.id, 'tipoErro', carregarTipos));
    });
}

async function aoCriarTipo(evento) {
    evento.preventDefault();
    const erroEl = document.getElementById('tipoErro');
    erroEl.textContent = '';
    const nome = document.getElementById('nomeTipo').value.trim();

    try {
        await chamarApi('/api/tipoDocumento', {
            method: 'POST',
            body: JSON.stringify({ nome })
        });
        document.getElementById('nomeTipo').value = '';
        await carregarTipos();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel criar: ' + erro.message;
    }
}

// ---------- Remoção genérica (com tratamento do 409 de FK) ----------

async function aoRemover(caminhoBase, id, idErro, recarregar) {
    const erroEl = document.getElementById(idErro);
    erroEl.textContent = '';
    try {
        await chamarApi(`/api/${caminhoBase}/${id}`, { method: 'DELETE' });
        await recarregar();
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel remover: ' + erro.message;
    }
}
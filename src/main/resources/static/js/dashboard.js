const TOKEN_KEY = 'gestaoDocumentos.jwt';
const USERNAME_KEY = 'gestaoDocumentos.username';

let graficoStatus;
let graficoPeriodo;

document.addEventListener('DOMContentLoaded', () => {
    const tokenSalvo = localStorage.getItem(TOKEN_KEY);
    if (tokenSalvo) {
        mostrarDashboard();
        carregarDashboard();
    } else {
        mostrarLogin();
    }

    document.getElementById('formLogin').addEventListener('submit', aoSubmeterLogin);
    document.getElementById('btnSair').addEventListener('click', logout);
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

        mostrarDashboard();
        await carregarDashboard();
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
    document.getElementById('dashboardSection').classList.add('escondido');
    document.getElementById('senha').value = '';
}

function mostrarDashboard() {
    document.getElementById('loginSection').classList.add('escondido');
    document.getElementById('dashboardSection').classList.remove('escondido');
    const username = localStorage.getItem(USERNAME_KEY);
    document.getElementById('usuarioLogado').textContent = username ? `Ola, ${username}` : '';
}

function obterToken() {
    return localStorage.getItem(TOKEN_KEY);
}

async function chamarApi(caminho) {
    const resposta = await fetch(caminho, {
        headers: { Authorization: `Bearer ${obterToken()}` }
    });

    if (resposta.status === 401 || resposta.status === 403) {
        // token expirado/invalido: desloga e manda pra tela de login de novo
        logout();
        throw new Error('Sessao expirada, faca login novamente');
    }

    if (!resposta.ok) {
        throw new Error(`Falha ao chamar ${caminho}: HTTP ${resposta.status}`);
    }

    return resposta.json();
}

async function carregarDashboard() {
    const erroEl = document.getElementById('mensagemErro');
    erroEl.textContent = '';

    try {
        const [porStatus, porPeriodo, documentos, tempoMedio] = await Promise.all([
            chamarApi('/api/solicitacoes/estatisticas/por-status'),
            chamarApi('/api/solicitacoes/estatisticas/por-periodo'),
            chamarApi('/api/solicitacoes/estatisticas/documentos-mais-solicitados'),
            chamarApi('/api/solicitacoes/estatisticas/tempo-medio-emissao')
        ]);

        preencherCards(porStatus, tempoMedio);
        desenharGraficoStatus(porStatus);
        desenharGraficoPeriodo(porPeriodo);
        preencherTabelaDocumentos(documentos);
    } catch (erro) {
        erroEl.textContent = 'Nao foi possivel carregar os dados: ' + erro.message;
    }
}

function preencherCards(porStatus, tempoMedio) {
    const total = porStatus.reduce((soma, item) => soma + item.quantidade, 0);
    document.getElementById('totalSolicitacoes').textContent = total;

    const emitida = porStatus.find(item => item.status === 'EMITIDA');
    document.getElementById('totalEmitidas').textContent = emitida ? emitida.quantidade : 0;

    const horas = tempoMedio.horasMedias ?? 0;
    document.getElementById('tempoMedio').textContent = horas.toFixed(1) + ' h';
}

function desenharGraficoStatus(dados) {
    const ctx = document.getElementById('graficoStatus');
    const labels = dados.map(item => item.status);
    const valores = dados.map(item => item.quantidade);

    if (graficoStatus) {
        graficoStatus.destroy();
    }
    graficoStatus = new Chart(ctx, {
        type: 'bar',
        data: {
            labels,
            datasets: [{ label: 'Solicitacoes', data: valores, backgroundColor: '#2b6cb0' }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
}

function desenharGraficoPeriodo(dados) {
    const ctx = document.getElementById('graficoPeriodo');
    const labels = dados.map(item => item.data);
    const valores = dados.map(item => item.quantidade);

    if (graficoPeriodo) {
        graficoPeriodo.destroy();
    }
    graficoPeriodo = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                label: 'Solicitacoes por dia',
                data: valores,
                borderColor: '#2f855a',
                backgroundColor: 'rgba(47, 133, 90, 0.15)',
                fill: true,
                tension: 0.25
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
}

function preencherTabelaDocumentos(dados) {
    const corpo = document.querySelector('#tabelaDocumentos tbody');
    corpo.innerHTML = '';
    dados.forEach(item => {
        const linha = document.createElement('tr');
        linha.innerHTML = `<td>${item.tipoDocumento}</td><td>${item.quantidade}</td>`;
        corpo.appendChild(linha);
    });
}
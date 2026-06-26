let currentUserInfo = null;
let tokenTimerId = null;

window.authReady = initializeAuth();

document.addEventListener('DOMContentLoaded', async () => {
    await window.authReady;

    bindStockSearch();
    bindMyInfoModal();
    bindTokenRefreshButton();
});

async function initializeAuth() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        redirectToLogin();
        return false;
    }

    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (isAuthError(response)) {
            redirectToLogin();
            return false;
        }

        if (!response.ok) {
            console.warn('사용자 정보 조회 실패:', response.status);
            return true;
        }

        currentUserInfo = await response.json();

        const userNickname = document.getElementById('user-nickname');

        if (userNickname) {
            userNickname.textContent = currentUserInfo.nickname || currentUserInfo.email || 'USER';
        }

        startTokenTimer();

        return true;
    } catch (error) {
        console.error('사용자 정보 조회 중 네트워크 오류:', error);
        return true;
    }
}

async function waitAuthReady() {
    if (!window.authReady) {
        window.authReady = initializeAuth();
    }

    return await window.authReady;
}

function redirectToLogin() {
    localStorage.removeItem('accessToken');
    window.location.replace('/login');
}

function isAuthError(response) {
    return response && (response.status === 401 || response.status === 403);
}

// 보호 API 요청 전에 인증 확인, Authorization 헤더 추가, 인증 실패 처리를 공통으로 수행한다.
async function authFetch(url, options = {}) {
    const authenticated = await waitAuthReady();

    if (!authenticated) {
        return null;
    }

    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        redirectToLogin();
        return null;
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (isAuthError(response)) {
        redirectToLogin();
        return null;
    }

    return response;
}

function bindStockSearch() {
    const form = document.getElementById('stock-search-form');
    const input = document.getElementById('stock-search-input');
    const results = document.getElementById('stock-search-results');

    if (!form || !input || !results) {
        return;
    }

    let timerId;

    input.addEventListener('input', () => {
        clearTimeout(timerId);

        const keyword = input.value.trim();

        if (!keyword) {
            clearSearchResults();
            return;
        }

        timerId = setTimeout(() => {
            searchStocks(keyword);
        }, 250);
    });

    form.addEventListener('submit', (event) => {
        event.preventDefault();

        const firstResult = results.querySelector('.search-result-item');

        if (firstResult) {
            firstResult.click();
            return;
        }

        const keyword = input.value.trim();

        if (keyword) {
            goStockDetail(keyword);
        }
    });

    document.addEventListener('click', (event) => {
        if (!form.contains(event.target)) {
            clearSearchResults();
        }
    });
}

async function searchStocks(keyword) {
    const response = await authFetch(`/api/stocks/search?keyword=${encodeURIComponent(keyword)}`);

    if (!response || !response.ok) {
        clearSearchResults();
        return;
    }

    const stocks = await response.json();
    renderSearchResults(stocks);
}

function renderSearchResults(stocks) {
    const results = document.getElementById('stock-search-results');

    if (!results) {
        return;
    }

    if (!stocks || stocks.length === 0) {
        results.innerHTML = `
            <div class="search-result-empty">검색 결과가 없습니다.</div>
        `;
        results.classList.add('active');
        return;
    }

    results.innerHTML = stocks.map((stock) => `
        <button type="button" class="search-result-item" onclick="goStockDetail('${escapeHtml(stock.symbol)}')">
            <span class="search-result-name">${escapeHtml(stock.name)}</span>
            <span class="search-result-meta">${escapeHtml(stock.symbol)} · ${escapeHtml(stock.market)}</span>
        </button>
    `).join('');

    results.classList.add('active');
}

function clearSearchResults() {
    const results = document.getElementById('stock-search-results');

    if (!results) {
        return;
    }

    results.innerHTML = '';
    results.classList.remove('active');
}

function goStockDetail(symbol) {
    window.location.href = `/stocks/detail?keyword=${encodeURIComponent(symbol)}`;
}

function handleLogout() {
    localStorage.removeItem('accessToken');
    window.location.href = '/login';
}

function bindMyInfoModal() {
    const profileButton = document.getElementById('profile-menu-btn');
    const myInfoOverlay = document.getElementById('my-info-modal-overlay');

    if (profileButton) {
        profileButton.addEventListener('click', showMyInfo);
    }

    if (myInfoOverlay) {
        myInfoOverlay.addEventListener('click', (event) => {
            if (event.target === myInfoOverlay) {
                closeMyInfoModal();
            }
        });
    }
}

function showMyInfo() {
    const overlay = document.getElementById('my-info-modal-overlay');

    if (!overlay || !currentUserInfo) {
        return;
    }

    setHeaderText('my-info-email', currentUserInfo.email || '-');
    setHeaderText('my-info-nickname', currentUserInfo.nickname || '-');

    overlay.classList.add('active');
}

function closeMyInfoModal() {
    const overlay = document.getElementById('my-info-modal-overlay');

    if (overlay) {
        overlay.classList.remove('active');
    }
}

function setHeaderText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function escapeHtml(value) {
    return String(value || '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function bindTokenRefreshButton() {
    const button = document.getElementById('token-refresh-btn');

    if (!button) {
        return;
    }

    button.addEventListener('click', refreshAccessTokenManually);
}

// 사용자가 연장 버튼을 눌렀을 때만 새 JWT를 발급받아 로그인 시간을 60분으로 되돌린다.
async function refreshAccessTokenManually() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        redirectToLogin();
        return;
    }

    const response = await fetch('/api/users/refresh', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (isAuthError(response)) {
        redirectToLogin();
        return;
    }

    if (!response.ok) {
        return;
    }

    const data = await response.json();

    if (data.token) {
        localStorage.setItem('accessToken', data.token);
        startTokenTimer();
    }
}

// JWT payload의 exp 값을 읽어 남은 로그인 시간을 계산한다.
function getTokenRemainingMs(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.exp * 1000 - Date.now();
    } catch (error) {
        return 0;
    }
}

function formatRemainingTime(milliseconds) {
    const totalSeconds = Math.max(0, Math.floor(milliseconds / 1000));
    const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0');
    const seconds = String(totalSeconds % 60).padStart(2, '0');

    return `${minutes}:${seconds}`;
}

// 헤더에 JWT 남은 시간을 표시하고 만료되면 로그인 화면으로 이동한다.
function startTokenTimer() {
    const remainTime = document.getElementById('token-remain-time');

    if (!remainTime) {
        return;
    }

    clearInterval(tokenTimerId);

    tokenTimerId = setInterval(() => {
        const token = localStorage.getItem('accessToken');
        const remainingMs = getTokenRemainingMs(token);

        remainTime.textContent = formatRemainingTime(remainingMs);

        if (remainingMs <= 0) {
            clearInterval(tokenTimerId);
            redirectToLogin();
        }
    }, 1000);
}
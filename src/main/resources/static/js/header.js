let currentUserInfo = null;

document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
    bindStockSearch();
    bindMyInfoModal();
});

async function checkLoginStatus() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        window.location.href = '/login';
        return;
    }

    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
            return;
        }

        currentUserInfo = await response.json();

        const userNickname = document.getElementById('user-nickname');

        if (userNickname) {
            userNickname.textContent = currentUserInfo.nickname || currentUserInfo.email || 'USER';
        }
    } catch (error) {
        console.error(error);
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
    }
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
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        return;
    }

    const response = await fetch(`/api/stocks/search?keyword=${encodeURIComponent(keyword)}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
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

document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
    bindStockSearch();
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

        const email = await response.text();
        const userEmail = document.getElementById('user-email');

        if (userEmail) {
            userEmail.textContent = email;
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
        <button type="button" class="search-result-item" onclick="goStockDetail('${escapeHtml(stock.name)}')">
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

function goStockDetail(stockName) {
    window.location.href = `/stocks/detail?keyword=${encodeURIComponent(stockName)}`;
}

function handleLogout() {
    localStorage.removeItem('accessToken');
    window.location.href = '/login';
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

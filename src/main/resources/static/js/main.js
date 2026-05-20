document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
    bindStockSearch();
    loadWatchlists();
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

    if (!form || !input) {
        return;
    }

    form.addEventListener('submit', (event) => {
        event.preventDefault();

        const keyword = input.value.trim();

        if (!keyword) {
            return;
        }

        window.location.href = `/stocks/detail?keyword=${encodeURIComponent(keyword)}`;
    });
}

function handleLogout() {
    localStorage.removeItem('accessToken');
    window.location.href = '/login';
}

async function loadWatchlists() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        return;
    }

    const response = await fetch('/api/watchlists', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        return;
    }

    const watchlists = await response.json();
    const emptyState = document.querySelector('.watchlist-panel .empty-state');

    if (!emptyState || watchlists.length === 0) {
        return;
    }

    emptyState.outerHTML = `
        <div class="watchlist-list">
            ${watchlists.map((item) => `
                <button type="button" class="watchlist-item" onclick="selectWatchlistStock('${escapeHtml(item.stockName)}')">
                    <span>${escapeHtml(item.stockName)}</span>
                </button>
            `).join('')}
        </div>
    `;
}

function goStockDetail(keyword) {
    window.location.href = `/stocks/detail?keyword=${keyword}`;
}

function escapeHtml(value) {
    return value
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function selectWatchlistStock(stockName) {
    renderMainChart(stockName);
    renderStockSideInfo(stockName);
}

function renderMainChart(stockName) {
    const chartPanel = document.querySelector('.chart-panel');

    if (!chartPanel) {
        return;
    }

    chartPanel.innerHTML = `
        <div class="panel-header chart-main-header">
            <div>
                <p class="panel-kicker">// SELECTED STOCK</p>
                <h1>${escapeHtml(stockName)}</h1>
            </div>

            <div class="chart-periods">
                <button type="button" class="active">1D</button>
                <button type="button">1W</button>
                <button type="button">1M</button>
                <button type="button">1Y</button>
            </div>
        </div>

        <div class="main-chart-box">
            <div class="chart-grid"></div>

            <svg class="mock-chart" viewBox="0 0 900 420" preserveAspectRatio="none">
                <g stroke="rgba(0,229,160,0.06)" stroke-width="1">
                    <line x1="0" y1="70" x2="900" y2="70"/>
                    <line x1="0" y1="140" x2="900" y2="140"/>
                    <line x1="0" y1="210" x2="900" y2="210"/>
                    <line x1="0" y1="280" x2="900" y2="280"/>
                    <line x1="0" y1="350" x2="900" y2="350"/>
                    <line x1="150" y1="0" x2="150" y2="420"/>
                    <line x1="300" y1="0" x2="300" y2="420"/>
                    <line x1="450" y1="0" x2="450" y2="420"/>
                    <line x1="600" y1="0" x2="600" y2="420"/>
                    <line x1="750" y1="0" x2="750" y2="420"/>
                </g>

                <defs>
                    <linearGradient id="mainChartArea" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stop-color="#00e5a0" stop-opacity="0.18"/>
                        <stop offset="100%" stop-color="#00e5a0" stop-opacity="0"/>
                    </linearGradient>
                </defs>

                <path d="M0,320 C80,280 120,300 190,235 S330,130 430,180 S590,245 700,120 S820,90 900,70 L900,420 L0,420 Z" fill="url(#mainChartArea)"/>
                <path class="chart-line" d="M0,320 C80,280 120,300 190,235 S330,130 430,180 S590,245 700,120 S820,90 900,70"/>
            </svg>

            <div class="chart-watermark">MOCK CHART</div>
        </div>
    `;
}

function renderStockSideInfo(stockName) {
    const sidePanel = document.querySelector('.side-panel');

    if (!sidePanel) {
        return;
    }

    sidePanel.innerHTML = `
        <div class="panel-header">
            <div>
                <p class="panel-kicker">// STOCK INFO</p>
                <h2>DETAILS</h2>
            </div>
        </div>

        <div class="stock-info-card primary">
            <span class="info-label">종목명</span>
            <strong>${escapeHtml(stockName)}</strong>
        </div>

        <div class="stock-info-grid">
            <div class="stock-info-card">
                <span class="info-label">현재가</span>
                <strong>-</strong>
            </div>
            <div class="stock-info-card">
                <span class="info-label">어제대비</span>
                <strong>-</strong>
            </div>
            <div class="stock-info-card">
                <span class="info-label">등락률</span>
                <strong>-</strong>
            </div>
            <div class="stock-info-card">
                <span class="info-label">거래량</span>
                <strong>-</strong>
            </div>
        </div>

        <section class="stock-info-section">
            <h3>종목정보</h3>
            <dl>
                <div>
                    <dt>시장</dt>
                    <dd>-</dd>
                </div>
                <div>
                    <dt>업종</dt>
                    <dd>-</dd>
                </div>
                <div>
                    <dt>시가총액</dt>
                    <dd>-</dd>
                </div>
                <div>
                    <dt>상장주식수</dt>
                    <dd>-</dd>
                </div>
            </dl>
        </section>

        <section class="stock-info-section">
            <h3>투자 참고</h3>
            <dl>
                <div>
                    <dt>PER</dt>
                    <dd>-</dd>
                </div>
                <div>
                    <dt>EPS</dt>
                    <dd>-</dd>
                </div>
                <div>
                    <dt>배당수익률</dt>
                    <dd>-</dd>
                </div>
            </dl>
        </section>
    `;
}
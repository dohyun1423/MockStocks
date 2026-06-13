let selectedWatchlistSymbol = null;
let draggedWatchlistId = null;

document.addEventListener('DOMContentLoaded', () => {
    bindDashboardTabs();
    loadWatchlists();
});

// 메인 화면의 관심종목/내 주식 탭 전환
function bindDashboardTabs() {
    const tabButtons = document.querySelectorAll('.dashboard-tabs button');

    tabButtons.forEach((button) => {
        button.addEventListener('click', async () => {
            const target = button.dataset.dashboardTab;

            switchDashboardTab(target);

            if (target === 'portfolio' && typeof loadPortfolioDashboard === 'function') {
                await loadPortfolioDashboard();
            }
        });
    });
}

// 메인 대시보드 탭을 공통으로 전환
function switchDashboardTab(target) {
    const tabButtons = document.querySelectorAll('.dashboard-tabs button');
    const panels = document.querySelectorAll('.dashboard-panel');

    tabButtons.forEach((button) => {
        button.classList.toggle('active', button.dataset.dashboardTab === target);
    });

    panels.forEach((panel) => {
        panel.classList.toggle('active', panel.id === `dashboard-${target}`);
    });
}

// 내 주식 탭에서 보유 종목을 선택하면 해당 종목 상세페이지로 이동
function openStockFromPortfolio(symbol) {
    if (!symbol) {
        return;
    }

    location.href = `/stocks/detail?keyword=${encodeURIComponent(symbol)}`;
}

// 내 관심 종목 리스트 불러오기
async function loadWatchlists() {
    const accessToken = localStorage.getItem('accessToken');
    const watchlistPanel = document.querySelector('.watchlist-panel');

    if (!accessToken || !watchlistPanel) {
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
    renderWatchlists(watchlists);

    // 첫 번째 관심종목을 메인 화면 진입 시 자동 선택한다.
    if (!selectedWatchlistSymbol && watchlists.length > 0) {
        const first = watchlists[0];
        const stockName = first.stockName || first.stock_name;

        selectedWatchlistSymbol = first.symbol || null;
        await selectWatchlistStock(stockName, first.symbol);
        markSelectedWatchlist(first.id);
    }
}

// 관심종목 목록 렌더링
function renderWatchlists(watchlists) {
    const watchlistPanel = document.querySelector('.watchlist-panel');
    const previousList = document.querySelector('.watchlist-list');
    const emptyState = document.querySelector('.watchlist-panel .empty-state');

    if (!watchlistPanel) {
        return;
    }

    previousList?.remove();

    if (!watchlists || watchlists.length === 0) {
        if (emptyState) {
            emptyState.style.display = 'flex';
        }
        return;
    }

    if (emptyState) {
        emptyState.style.display = 'none';
    }

    const list = document.createElement('div');
    list.className = 'watchlist-list';

    watchlists.forEach((item) => {
        const stockName = item.stockName || item.stock_name;
        const symbol = item.symbol;

        if (!stockName) {
            return;
        }

        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'watchlist-item';
        button.textContent = stockName;
        button.draggable = true;
        button.dataset.watchlistId = item.id;
        button.dataset.symbol = symbol || '';
        button.dataset.stockName = stockName;

        if (selectedWatchlistSymbol && normalizeMainSymbol(symbol) === normalizeMainSymbol(selectedWatchlistSymbol)) {
            button.classList.add('active');
        }

        button.addEventListener('click', async () => {
            selectedWatchlistSymbol = symbol || null;
            markSelectedWatchlist(item.id);
            await selectWatchlistStock(stockName, symbol);
        });

        button.addEventListener('dragstart', () => {
            draggedWatchlistId = item.id;
            button.classList.add('dragging');
        });

        button.addEventListener('dragend', () => {
            draggedWatchlistId = null;
            button.classList.remove('dragging');
        });

        button.addEventListener('dragover', (event) => {
            event.preventDefault();

            const draggingButton = list.querySelector('.watchlist-item.dragging');

            if (!draggingButton || draggingButton === button) {
                return;
            }

            const rect = button.getBoundingClientRect();
            const shouldInsertAfter = event.clientY > rect.top + rect.height / 2;

            if (shouldInsertAfter) {
                button.after(draggingButton);
                return;
            }

            button.before(draggingButton);
        });

        button.addEventListener('drop', async (event) => {
            event.preventDefault();
            await saveWatchlistOrder();
        });

        list.appendChild(button);
    });

    watchlistPanel.appendChild(list);
}

function markSelectedWatchlist(watchlistId) {
    const buttons = document.querySelectorAll('.watchlist-item');

    buttons.forEach((button) => {
        button.classList.toggle(
            'active',
            String(button.dataset.watchlistId) === String(watchlistId)
        );
    });
}

async function saveWatchlistOrder() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        return;
    }

    const ids = Array.from(document.querySelectorAll('.watchlist-item'))
        .map((button) => Number(button.dataset.watchlistId))
        .filter((id) => Number.isFinite(id));

    await fetch('/api/watchlists/order', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
        },
        body: JSON.stringify({
            watchlistIds: ids
        })
    });
}

function normalizeMainSymbol(value) {
    return String(value || '')
        .replace(/\s+/g, '')
        .toUpperCase();
}

// 관심종목 클릭 시 종목 상세, 현재가, 가격 이력을 조회해서 화면 갱신
async function selectWatchlistStock(stockName, symbol = null) {
    const stock = symbol
        ? await fetchStockDetailBySymbol(symbol)
        : await fetchStockDetail(stockName);

    if (!stock) {
        renderMainChart(stockName, [], null, '1D');
        renderStockSideInfo(stockName, null, null);
        return;
    }

    const quote = await fetchStockQuote(stock.symbol);
    const priceHistories = await fetchStockPriceHistories(stock.symbol, '1D');

    renderMainChart(stock.name, priceHistories, stock.symbol, '1D', quote);
    renderStockSideInfo(stock.name, stock, quote);
}

async function fetchStockDetail(stockName) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        return null;
    }

    const response = await fetch(`/api/stocks/detail?name=${encodeURIComponent(stockName)}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        return null;
    }

    return await response.json();
}

// 종목코드로 종목 상세 정보 조회
async function fetchStockDetailBySymbol(symbol) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken || !symbol) {
        return null;
    }

    const response = await fetch(`/api/stocks/symbol/${encodeURIComponent(symbol)}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        return null;
    }

    return await response.json();
}

async function fetchStockQuote(symbol) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken || !symbol) {
        return null;
    }

    const response = await fetch(`/api/stocks/${encodeURIComponent(symbol)}/quote`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        return null;
    }

    return await response.json();
}

// 종목코드와 기간으로 차트용 가격 이력 조회
async function fetchStockPriceHistories(symbol, period) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken || !symbol) {
        return [];
    }

    const response = await fetch(`/api/stocks/${encodeURIComponent(symbol)}/prices?period=${encodeURIComponent(period)}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        return [];
    }

    return await response.json();
}

// 현재가 quote의 등락값을 기준으로 메인 차트 상승/하락 색상을 결정한다.
function resolveMainChartClass(quote, hasChartData, firstPrice, latestPrice) {
    const quoteChangeValue = quote?.changePrice ?? quote?.changeRate;
    const quoteChangeNumber = Number(quoteChangeValue);

    if (quoteChangeValue !== null && quoteChangeValue !== undefined && !Number.isNaN(quoteChangeNumber)) {
        return quoteChangeNumber < 0 ? 'down' : 'up';
    }

    return hasChartData && latestPrice < firstPrice ? 'down' : 'up';
}

// 가격 이력 데이터로 메인 차트 영역 갱신
function renderMainChart(stockName, priceHistories = [], symbol = null, activePeriod = '1D', quote = null) {
    const chartPanel = document.querySelector('.chart-panel');

    if (!chartPanel) {
        return;
    }

    const points = createChartPoints(priceHistories);
    const linePath = createLinePath(points);
    const areaPath = createAreaPath(points);
    // 차트 데이터가 있을 때만 상승/하락 색상을 계산한다.
    const hasChartData = points.length > 0 && Array.isArray(priceHistories) && priceHistories.length > 0;
    const firstPrice = hasChartData ? Number(priceHistories[0]?.closePrice || 0) : 0;
    const latestPrice = hasChartData ? Number(priceHistories[priceHistories.length - 1]?.closePrice || 0) : 0;
    // 메인 화면의 정보 카드와 차트 색상이 같은 등락 기준을 쓰도록 quote를 우선 사용한다.
    const chartClass = resolveMainChartClass(quote, hasChartData, firstPrice, latestPrice);
    const chartColor = chartClass === 'down' ? '#3b82f6' : '#ff4560';
    const chartAreaId = chartClass === 'down' ? 'mainChartAreaDown' : 'mainChartAreaUp';

    chartPanel.innerHTML = `
        <div class="panel-header chart-main-header">
            <div class="chart-title-row">
                <div>
                    <p class="panel-kicker">// SELECTED STOCK</p>
                    <h1>${escapeHtml(stockName)}</h1>
                </div>

                ${
                    symbol
                        ? `
                            <div class="chart-action-buttons">
                                <button
                                    type="button"
                                    class="chart-trade-btn buy"
                                    data-symbol="${escapeHtml(symbol)}"
                                    data-stock-name="${escapeHtml(stockName)}"
                                    data-order-type="BUY"
                                >
                                    매수
                                </button>

                                <button
                                    type="button"
                                    class="chart-trade-btn sell"
                                    data-symbol="${escapeHtml(symbol)}"
                                    data-stock-name="${escapeHtml(stockName)}"
                                    data-order-type="SELL"
                                >
                                    매도
                                </button>

                                <button
                                    type="button"
                                    class="chart-watchlist-btn active"
                                    data-symbol="${escapeHtml(symbol)}"
                                    data-stock-name="${escapeHtml(stockName)}"
                                    aria-label="관심종목"
                                >
                                    ♥
                                </button>
                            </div>
                        `
                        : ''
                }
            </div>

            <div class="chart-periods">
                <button type="button" data-period="1D" class="${activePeriod === '1D' ? 'active' : ''}">1D</button>
                <button type="button" data-period="1W" class="${activePeriod === '1W' ? 'active' : ''}">1W</button>
                <button type="button" data-period="1M" class="${activePeriod === '1M' ? 'active' : ''}">1M</button>
                <button type="button" data-period="1Y" class="${activePeriod === '1Y' ? 'active' : ''}">1Y</button>
            </div>
        </div>

        <div class="main-chart-box">
            <div class="chart-grid"></div>

            ${
                points.length > 0
                    ? `
                        <svg class="mock-chart" viewBox="0 0 900 420" preserveAspectRatio="none">
                            <defs>
                                <linearGradient id="mainChartAreaUp" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stop-color="#ff4560" stop-opacity="0.18"/>
                                    <stop offset="100%" stop-color="#ff4560" stop-opacity="0"/>
                                </linearGradient>

                                <linearGradient id="mainChartAreaDown" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.18"/>
                                    <stop offset="100%" stop-color="#3b82f6" stop-opacity="0"/>
                                </linearGradient>
                            </defs>

                            <path d="${areaPath}" fill="url(#${chartAreaId})"></path>
                            <path
                                class="chart-line ${chartClass}"
                                d="${linePath}"
                                style="stroke: ${chartColor};"
                            ></path>
                        </svg>

                        <div class="chart-watermark">PRICE HISTORY</div>
                    `
                    : `
                        <div class="chart-placeholder">
                            <p>가격 이력이 없습니다.</p>
                            <span>차트에 표시할 데이터를 찾을 수 없습니다.</span>
                        </div>
                    `
            }
        </div>
    `;

    bindChartPeriodButtons(stockName, symbol, quote);
    bindChartActionButtons();
}

// 차트 상단의 매수, 매도, 관심 버튼 연결
function bindChartActionButtons() {
    const tradeButtons = document.querySelectorAll('.chart-trade-btn');

    tradeButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const symbol = button.dataset.symbol;
            const stockName = button.dataset.stockName;
            const orderType = button.dataset.orderType;

            if (!symbol || !stockName || !orderType) {
                return;
            }

            openOrderModal(symbol, stockName, orderType);
        });
    });

    const watchlistButton = document.querySelector('.chart-watchlist-btn');

    if (watchlistButton) {
        watchlistButton.addEventListener('click', async () => {
            const stockName = watchlistButton.dataset.stockName;

            if (!stockName) {
                return;
            }

            const success = await addMainWatchlist(stockName);

            if (success) {
                watchlistButton.classList.add('active');
                await loadWatchlists();
            }
        });
    }
}

// 메인 차트에서 관심종목 추가
async function addMainWatchlist(stockName) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken || !stockName) {
        return false;
    }

    const response = await fetch('/api/watchlists', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
        },
        body: JSON.stringify({
            stockName: stockName
        })
    });

    return response.ok;
}

// 차트 기간 버튼 클릭 시 같은 quote 기준으로 상승/하락 색상을 유지한다.
function bindChartPeriodButtons(stockName, symbol, quote = null) {
    const buttons = document.querySelectorAll('.chart-periods button');

    buttons.forEach((button) => {
        button.addEventListener('click', async () => {
            const period = button.dataset.period;

            if (!symbol || !period) {
                return;
            }

            const priceHistories = await fetchStockPriceHistories(symbol, period);
            renderMainChart(stockName, priceHistories, symbol, period, quote);
        });
    });
}

// 가격 이력을 SVG 좌표로 변환
function createChartPoints(priceHistories) {
    if (!priceHistories || priceHistories.length === 0) {
        return [];
    }

    const width = 900;
    const height = 420;
    const padding = 36;
    const prices = priceHistories.map((item) => Number(item.closePrice));
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    const priceRange = maxPrice - minPrice || 1;

    return priceHistories.map((item, index) => {
        const price = Number(item.closePrice);
        const x = padding + index * ((width - padding * 2) / Math.max(priceHistories.length - 1, 1));
        const y = height - padding - ((price - minPrice) / priceRange) * (height - padding * 2);

        return { x, y };
    });
}

// SVG 선 차트 path 생성
function createLinePath(points) {
    return points
        .map((point, index) => {
            const command = index === 0 ? 'M' : 'L';
            return `${command}${point.x.toFixed(1)},${point.y.toFixed(1)}`;
        })
        .join(' ');
}

// SVG 영역 차트 path 생성
function createAreaPath(points) {
    if (points.length === 0) {
        return '';
    }

    const linePath = createLinePath(points);
    const firstPoint = points[0];
    const lastPoint = points[points.length - 1];

    return `${linePath} L${lastPoint.x.toFixed(1)},420 L${firstPoint.x.toFixed(1)},420 Z`;
}

function renderStockSideInfo(stockName, stock, quote) {
    const sidePanel = document.querySelector('.side-panel');

    if (!sidePanel) {
        return;
    }

    if (!stock) {
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

            <section class="stock-info-section">
                <h3>정보 없음</h3>
                <p class="stock-info-message">등록된 종목 정보를 찾을 수 없습니다.</p>
            </section>
        `;
        return;
    }

    const price = quote || stock;

    sidePanel.innerHTML = `
        <div class="panel-header">
            <div>
                <p class="panel-kicker">// STOCK INFO</p>
                <h2>DETAILS</h2>
            </div>
        </div>

        <div class="stock-info-card primary">
            <span class="info-label">종목명</span>
            <strong>${escapeHtml(stock.name)}</strong>
        </div>

        <div class="stock-info-grid">
            <div class="stock-info-card">
                <span class="info-label">현재가</span>
                <strong>${formatNumber(price.currentPrice)}원</strong>
            </div>
            <div class="stock-info-card">
                <span class="info-label">어제대비</span>
                <strong class="${price.changePrice >= 0 ? 'up' : 'down'}">${formatSignedNumber(price.changePrice)}원</strong>
            </div>
            <div class="stock-info-card">
                <span class="info-label">등락률</span>
                <strong class="${price.changeRate >= 0 ? 'up' : 'down'}">${formatSignedNumber(price.changeRate)}%</strong>
            </div>
            <div class="stock-info-card">
                <span class="info-label">거래량</span>
                <strong>${formatNumber(price.volume)}</strong>
            </div>
        </div>

        <section class="stock-info-section">
            <h3>종목정보</h3>
            <dl>
                <div><dt>종목코드</dt><dd>${escapeHtml(stock.symbol)}</dd></div>
                <div><dt>시장</dt><dd>${escapeHtml(stock.market)}</dd></div>
                <div><dt>업종</dt><dd>${escapeHtml(stock.sector)}</dd></div>
                <div><dt>시가총액</dt><dd>${formatNumber(stock.marketCap)}원</dd></div>
                <div><dt>상장주식수</dt><dd>${formatNumber(stock.listedShares)}</dd></div>
            </dl>
        </section>

        <section class="stock-info-section">
            <h3>실시간 참고</h3>
            <dl>
                <div><dt>시가</dt><dd>${formatNumber(quote?.openPrice)}원</dd></div>
                <div><dt>고가</dt><dd>${formatNumber(quote?.highPrice)}원</dd></div>
                <div><dt>저가</dt><dd>${formatNumber(quote?.lowPrice)}원</dd></div>
                <div><dt>거래대금</dt><dd>${formatNumber(quote?.tradingValue)}원</dd></div>
            </dl>
        </section>
    `;
}

function formatNumber(value) {
    if (value === null || value === undefined) {
        return '-';
    }

    return Number(value).toLocaleString('ko-KR');
}

function formatSignedNumber(value) {
    if (value === null || value === undefined) {
        return '-';
    }

    const number = Number(value);
    const sign = number > 0 ? '+' : '';

    return `${sign}${number.toLocaleString('ko-KR')}`;
}

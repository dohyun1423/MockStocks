document.addEventListener('DOMContentLoaded', () => {
    loadWatchlists();
});

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

    const list = document.createElement('div');
    list.className = 'watchlist-list';

    watchlists.forEach((item) => {
        const stockName = item.stockName || item.stock_name;

        if (!stockName) {
            return;
        }

        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'watchlist-item';
        button.textContent = stockName;

        button.addEventListener('click', () => {
            selectWatchlistStock(stockName);
        });

        list.appendChild(button);
    });

    emptyState.replaceWith(list);
}

// 관심종목 클릭 시 종목 상세, 현재가, 가격 이력을 조회해서 화면 갱신
async function selectWatchlistStock(stockName) {
    const stock = await fetchStockDetail(stockName);

    if (!stock) {
        renderMainChart(stockName, [], null, '1M');
        renderStockSideInfo(stockName, null, null);
        return;
    }

    const quote = await fetchStockQuote(stock.symbol);
    const priceHistories = await fetchStockPriceHistories(stock.symbol, '1D');

    renderMainChart(stock.name, priceHistories, stock.symbol, '1D');
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

// 가격 이력 데이터로 메인 차트 영역 갱신
function renderMainChart(stockName, priceHistories = [], symbol = null, activePeriod = '1M') {
    const chartPanel = document.querySelector('.chart-panel');

    if (!chartPanel) {
        return;
    }

    const points = createChartPoints(priceHistories);
    const linePath = createLinePath(points);
    const areaPath = createAreaPath(points);

    chartPanel.innerHTML = `
        <div class="panel-header chart-main-header">
            <div>
                <p class="panel-kicker">// SELECTED STOCK</p>
                <h1>${escapeHtml(stockName)}</h1>
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
                                <linearGradient id="mainChartArea" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stop-color="#00e5a0" stop-opacity="0.18"/>
                                    <stop offset="100%" stop-color="#00e5a0" stop-opacity="0"/>
                                </linearGradient>
                            </defs>

                            <path d="${areaPath}" fill="url(#mainChartArea)"></path>
                            <path class="chart-line" d="${linePath}"></path>
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

    bindChartPeriodButtons(stockName, symbol);
}

// 차트 기간 버튼 클릭 시 해당 기간의 가격 이력 다시 조회
function bindChartPeriodButtons(stockName, symbol) {
    const buttons = document.querySelectorAll('.chart-periods button');

    buttons.forEach((button) => {
        button.addEventListener('click', async () => {
            const period = button.dataset.period;

            if (!symbol || !period) {
                return;
            }

            const priceHistories = await fetchStockPriceHistories(symbol, period);
            renderMainChart(stockName, priceHistories, symbol, period);
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

// 주식 상세 화면에서 관심종목, 현재가, 내 주식 정보를 처리하는 스크립트

let currentStock = null;
let stockDetailReady = null;
let detailMyStockLoaded = false;
let detailMyStockLoading = false;

document.addEventListener('DOMContentLoaded', async () => {
    const authenticated = await waitAuthReady();

    if (!authenticated) {
        return;
    }

    bindStockTabs();
    bindFavoriteButton();
    bindDetailOrderButtons();

    stockDetailReady = initStockDetail();
    await stockDetailReady;
});

// 상세페이지 매수/매도 버튼을 주문 모달과 연결
function bindDetailOrderButtons() {
    const buyButton = document.getElementById('detail-buy-btn');
    const sellButton = document.getElementById('detail-sell-btn');

    buyButton?.addEventListener('click', () => {
        openDetailOrderModal('BUY');
    });

    sellButton?.addEventListener('click', () => {
        openDetailOrderModal('SELL');
    });
}

// 현재 상세 종목 기준으로 주문 모달 열기
function openDetailOrderModal(orderType) {
    if (!currentStock || !currentStock.symbol) {
        return;
    }

    openOrderModal(currentStock.symbol, currentStock.name, orderType);
}

// 상세페이지 초기 데이터 조회
async function initStockDetail() {
    const keyword = getTitleStockName();

    if (!keyword) {
        return;
    }

    currentStock = await fetchStockDetail(keyword);

    if (!currentStock) {
        renderDetailMyStockError('종목 정보를 찾지 못했습니다.');
        return;
    }

    setStockTitle(currentStock.name);

    const quote = await fetchStockQuote(currentStock.symbol);

    if (quote) {
        renderStockQuote(quote);
    }

    renderDetailStockInfo(currentStock, quote);
    await loadFavoriteStatus(currentStock);
}

// 상세페이지 내 주식 탭에 현재 종목의 보유 정보와 거래내역 표시
async function renderDetailMyStock(symbol) {
    const wrap = document.getElementById('detail-my-stock-wrap');

    if (!wrap || !symbol) {
        return false;
    }

    const [portfolio, trades] = await Promise.all([
        fetchMyPortfolio(),
        fetchMyTrades(symbol)
    ]);

    if (!portfolio) {
        renderDetailMyStockError('내 주식 정보를 불러오지 못했습니다.');
        return false;
    }

    const holding = (portfolio.holdings || []).find((item) => {
        return normalizeStockSymbol(item.symbol) === normalizeStockSymbol(symbol);
    });

    wrap.innerHTML = `
        ${renderDetailHoldingSection(holding)}
        ${renderDetailTradeSection(trades || [])}
    `;

    return true;
}

// 내 포트폴리오 조회
async function fetchMyPortfolio() {
    const authenticated = await waitAuthReady();

    if (!authenticated) {
        return null;
    }

    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        redirectToLogin();
        return null;
    }

    const response = await fetch('/api/portfolio', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (isAuthError(response)) {
        redirectToLogin();
        return null;
    }

    if (!response.ok) {
        return null;
    }

    return await response.json();
}

// 현재 종목의 거래내역 조회
async function fetchMyTrades(symbol) {
    const authenticated = await waitAuthReady();

    if (!authenticated) {
        return [];
    }

    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        redirectToLogin();
        return [];
    }

    const query = symbol ? `?symbol=${encodeURIComponent(symbol)}` : '';
    const response = await fetch(`/api/trades${query}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (isAuthError(response)) {
        redirectToLogin();
        return [];
    }

    if (!response.ok) {
        return [];
    }

    return await response.json();
}

// 현재 종목의 보유 정보를 카드 형태로 렌더링
function renderDetailHoldingSection(holding) {
    if (!holding) {
        return `
            <section class="detail-my-section">
                <h3>보유 정보</h3>

                <div class="detail-my-empty">
                    <p>현재 보유 중인 수량이 없습니다.</p>
                    <span>매수를 진행하면 이 영역에 보유 정보가 표시됩니다.</span>
                </div>
            </section>
        `;
    }

    return `
        <section class="detail-my-section">
            <h3>보유 정보</h3>

            <div class="detail-my-cards">
                <div class="detail-my-card">
                    <span>보유수량</span>
                    <strong>${formatNumber(holding.quantity)}주</strong>
                </div>

                <div class="detail-my-card">
                    <span>평균단가</span>
                    <strong>${formatNumber(holding.averagePrice)}원</strong>
                </div>

                <div class="detail-my-card">
                    <span>현재가</span>
                    <strong>${formatNumber(holding.currentPrice)}원</strong>
                </div>

                <div class="detail-my-card">
                    <span>평가금액</span>
                    <strong>${formatNumber(holding.evaluationAmount)}원</strong>
                </div>

                <div class="detail-my-card">
                    <span>손익</span>
                    <strong class="${Number(holding.profitLoss) >= 0 ? 'up' : 'down'}">
                        ${formatSignedNumber(holding.profitLoss)}원
                    </strong>
                </div>

                <div class="detail-my-card">
                    <span>수익률</span>
                    <strong class="${Number(holding.profitRate) >= 0 ? 'up' : 'down'}">
                        ${formatSignedNumber(holding.profitRate)}%
                    </strong>
                </div>
            </div>
        </section>
    `;
}

// 현재 종목의 거래내역을 표로 렌더링
function renderDetailTradeSection(trades) {
    if (!trades || trades.length === 0) {
        return `
            <section class="detail-my-section">
                <h3>거래내역</h3>

                <div class="detail-my-empty">
                    <p>거래내역이 없습니다.</p>
                    <span>이 종목을 매수하거나 매도하면 거래내역이 표시됩니다.</span>
                </div>
            </section>
        `;
    }

    return `
        <section class="detail-my-section">
            <h3>거래내역</h3>

            <table class="detail-trade-table">
                <thead>
                <tr>
                    <th>구분</th>
                    <th>수량</th>
                    <th>체결가</th>
                    <th>거래금액</th>
                    <th>거래시간</th>
                </tr>
                </thead>
                <tbody>
                ${trades.map((trade) => `
                    <tr>
                        <td>
                            <span class="detail-trade-type ${trade.orderType === 'BUY' ? 'buy' : 'sell'}">
                                ${trade.orderType === 'BUY' ? '매수' : '매도'}
                            </span>
                        </td>
                        <td>${formatNumber(trade.quantity)}주</td>
                        <td>${formatNumber(trade.price)}원</td>
                        <td>${formatNumber(trade.totalAmount)}원</td>
                        <td>${formatTradeDate(trade.tradedAt)}</td>
                    </tr>
                `).join('')}
                </tbody>
            </table>
        </section>
    `;
}

// 내 주식 조회 실패 시 표시
function renderDetailMyStockError(message = '내 주식 정보를 불러오지 못했습니다.') {
    const wrap = document.getElementById('detail-my-stock-wrap');

    if (!wrap) {
        return;
    }

    wrap.innerHTML = `
        <div class="detail-my-empty">
            <p>${escapeHtml(message)}</p>
            <span>잠시 후 다시 시도해 주세요.</span>
        </div>
    `;
}

// 종목정보 탭에 현재 종목의 상세 정보 표시
function renderDetailStockInfo(stock, quote) {
    const infoGrid = document.getElementById('detail-info-grid');

    if (!infoGrid || !stock) {
        return;
    }

    const price = quote || stock;

    infoGrid.innerHTML = `
        <section class="detail-info-section">
            <h3>가격 정보</h3>

            <div class="detail-info-cards">
                <div class="detail-info-card">
                    <span>현재가</span>
                    <strong>${formatNumber(price.currentPrice)}원</strong>
                </div>

                <div class="detail-info-card">
                    <span>어제대비</span>
                    <strong class="${Number(price.changePrice) >= 0 ? 'up' : 'down'}">
                        ${formatSignedNumber(price.changePrice)}원
                    </strong>
                </div>

                <div class="detail-info-card">
                    <span>등락률</span>
                    <strong class="${Number(price.changeRate) >= 0 ? 'up' : 'down'}">
                        ${formatSignedNumber(price.changeRate)}%
                    </strong>
                </div>

                <div class="detail-info-card">
                    <span>거래량</span>
                    <strong>${formatNumber(price.volume)}</strong>
                </div>
            </div>
        </section>

        <section class="detail-info-section">
            <h3>종목정보</h3>

            <dl class="detail-info-list">
                <div>
                    <dt>종목명</dt>
                    <dd>${escapeHtml(stock.name)}</dd>
                </div>
                <div>
                    <dt>종목코드</dt>
                    <dd>${escapeHtml(stock.symbol)}</dd>
                </div>
                <div>
                    <dt>시장</dt>
                    <dd>${escapeHtml(stock.market)}</dd>
                </div>
                <div>
                    <dt>업종</dt>
                    <dd>${escapeHtml(stock.sector)}</dd>
                </div>
                <div>
                    <dt>시가총액</dt>
                    <dd>${formatNumber(stock.marketCap)}원</dd>
                </div>
                <div>
                    <dt>상장주식수</dt>
                    <dd>${formatNumber(stock.listedShares)}</dd>
                </div>
                <div>
                    <dt>PER</dt>
                    <dd>${formatNumber(stock.per)}</dd>
                </div>
                <div>
                    <dt>EPS</dt>
                    <dd>${formatNumber(stock.eps)}원</dd>
                </div>
                <div>
                    <dt>배당수익률</dt>
                    <dd>${formatNumber(stock.dividendYield)}%</dd>
                </div>
            </dl>
        </section>

        <section class="detail-info-section">
            <h3>실시간 참고</h3>

            <dl class="detail-info-list">
                <div>
                    <dt>시가</dt>
                    <dd>${formatNumber(quote?.openPrice)}원</dd>
                </div>
                <div>
                    <dt>고가</dt>
                    <dd>${formatNumber(quote?.highPrice)}원</dd>
                </div>
                <div>
                    <dt>저가</dt>
                    <dd>${formatNumber(quote?.lowPrice)}원</dd>
                </div>
                <div>
                    <dt>기준가</dt>
                    <dd>${formatNumber(quote?.basePrice)}원</dd>
                </div>
                <div>
                    <dt>거래대금</dt>
                    <dd>${formatNumber(quote?.tradingValue)}원</dd>
                </div>
            </dl>
        </section>
    `;
}

// 상세 화면 탭 전환 처리
function bindStockTabs() {
    const tabButtons = document.querySelectorAll('.stock-tabs button');
    const tabPanels = document.querySelectorAll('.tab-panel');

    tabButtons.forEach((button) => {
        button.addEventListener('click', async () => {
            const target = button.dataset.tab;

            tabButtons.forEach((item) => item.classList.remove('active'));
            tabPanels.forEach((panel) => panel.classList.remove('active'));

            button.classList.add('active');

            const targetPanel = document.getElementById(`tab-${target}`);

            if (targetPanel) {
                targetPanel.classList.add('active');
            }

            if (target === 'my-stock') {
                await loadDetailMyStockTab();
            }
        });
    });
}

async function loadDetailMyStockTab(force = false) {
    if (detailMyStockLoaded && !force) {
        return;
    }

    if (detailMyStockLoading) {
        return;
    }

    detailMyStockLoading = true;

    try {
        if (stockDetailReady) {
            await stockDetailReady;
        }

        if (!currentStock?.symbol) {
            renderDetailMyStockError('종목 정보가 아직 준비되지 않았습니다.');
            return;
        }

        const success = await renderDetailMyStock(currentStock.symbol);

        if (success) {
            detailMyStockLoaded = true;
        }
    } finally {
        detailMyStockLoading = false;
    }
}

// 관심종목 하트 클릭 시 추가 또는 삭제 처리
function bindFavoriteButton() {
    const favoriteButton = document.getElementById('favorite-btn');

    if (!favoriteButton) {
        return;
    }

    favoriteButton.addEventListener('click', async () => {
        const stockName = getCurrentStockName();

        if (!stockName) {
            return;
        }

        if (favoriteButton.classList.contains('active')) {
            const success = await removeWatchlist(stockName);

            if (success) {
                favoriteButton.classList.remove('active');
            }

            return;
        }

        const success = await addWatchlist(stockName);

        if (success) {
            favoriteButton.classList.add('active');
        }
    });
}

// DB 관심종목 목록을 조회해서 현재 종목의 하트 상태 반영
async function loadFavoriteStatus(stock) {
    const favoriteButton = document.getElementById('favorite-btn');
    const accessToken = localStorage.getItem('accessToken');

    if (!favoriteButton || !accessToken || !stock) {
        return;
    }

    const targetSymbol = normalizeStockSymbol(stock.symbol);
    const targetNames = [stock.name, stock.symbol, getTitleStockName()]
        .map(normalizeStockName)
        .filter((name) => name.length > 0);

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

    const exists = watchlists.some((item) => {
        const itemSymbol = normalizeStockSymbol(item.symbol);
        const itemStockName = normalizeStockName(item.stockName || item.stock_name);

        return (targetSymbol && itemSymbol === targetSymbol)
            || targetNames.includes(itemStockName);
    });

    favoriteButton.classList.toggle('active', exists);
}

// 관심종목 DB 저장
async function addWatchlist(stockName) {
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

// 관심종목 DB 삭제
async function removeWatchlist(stockName) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken || !stockName) {
        return false;
    }

    const response = await fetch(`/api/watchlists?stockName=${encodeURIComponent(stockName)}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    return response.ok;
}

// 종목명 또는 종목코드로 DB 상세 정보 조회
async function fetchStockDetail(keyword) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        return null;
    }

    const normalizedKeyword = String(keyword || '').trim();
    const isSymbol = /^[0-9A-Z]+$/.test(normalizedKeyword);

    const url = isSymbol
        ? `/api/stocks/symbol/${encodeURIComponent(normalizedKeyword)}`
        : `/api/stocks/detail?name=${encodeURIComponent(normalizedKeyword)}`;

    const response = await fetch(url, {
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
// 종목코드로 현재가 조회
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

// quote 응답을 상세 화면 가격 카드에 표시
function renderStockQuote(quote) {
    setText('detail-current-price', `${formatNumber(quote.currentPrice)}원`);
    setText('detail-change-price', `${formatSignedNumber(quote.changePrice)}원`);
    setText('detail-change-rate', `${formatSignedNumber(quote.changeRate)}%`);
    setText('detail-volume', formatNumber(quote.volume));

    setPriceColor('detail-change-price', quote.changePrice);
    setPriceColor('detail-change-rate', quote.changeRate);
}

// 상세 화면 제목의 현재 종목명 조회
function getTitleStockName() {
    const stockTitle = document.querySelector('.stock-title');

    if (!stockTitle) {
        return '';
    }

    return stockTitle.textContent.trim();
}

// 현재 페이지에서 사용할 기준 종목명 조회
function getCurrentStockName() {
    if (currentStock && currentStock.name) {
        return currentStock.name;
    }

    return getTitleStockName();
}

// 상세 화면 제목을 DB 기준 종목명으로 보정
function setStockTitle(stockName) {
    const stockTitle = document.querySelector('.stock-title');

    if (stockTitle) {
        stockTitle.textContent = stockName;
    }
}

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

// 등락 값에 따라 상승/하락 색상 적용
function setPriceColor(id, value) {
    const element = document.getElementById(id);

    if (!element) {
        return;
    }

    element.classList.remove('up', 'down');

    if (Number(value) > 0) {
        element.classList.add('up');
        return;
    }

    if (Number(value) < 0) {
        element.classList.add('down');
    }
}

// 관심종목 비교를 위해 공백 제거 후 대문자로 통일
function normalizeStockName(value) {
    return String(value || '')
        .replace(/\s+/g, '')
        .toUpperCase();
}

// 종목코드 비교를 위해 공백 제거 후 대문자로 통일
function normalizeStockSymbol(value) {
    return String(value || '')
        .replace(/\s+/g, '')
        .toUpperCase();
}

function escapeHtml(value) {
    return String(value || '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function formatNumber(value) {
    if (value === null || value === undefined || value === '') {
        return '-';
    }

    return Number(value).toLocaleString('ko-KR');
}

function formatSignedNumber(value) {
    if (value === null || value === undefined || value === '') {
        return '-';
    }

    const number = Number(value);
    const sign = number > 0 ? '+' : '';

    return `${sign}${number.toLocaleString('ko-KR')}`;
}

function formatTradeDate(value) {
    if (!value) {
        return '-';
    }

    return String(value).replace('T', ' ').slice(0, 16);
}

// 상세페이지에서 주문 성공 시 현재 종목의 가격, 종목정보, 보유 정보, 거래내역을 다시 조회
window.handleOrderSuccess = async function () {
    if (!currentStock || !currentStock.symbol) {
        return;
    }

    const quote = await fetchStockQuote(currentStock.symbol);

    if (quote) {
        renderStockQuote(quote);
        renderDetailStockInfo(currentStock, quote);
    }

    detailMyStockLoaded = false;
    await loadDetailMyStockTab(true);
};

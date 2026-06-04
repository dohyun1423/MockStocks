document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('holding-table-wrap')) {
        loadPortfolioDashboard();
    }
});

// 내 주식 탭에서 포트폴리오와 거래내역을 함께 갱신
async function loadPortfolioDashboard() {
    await loadPortfolio();
    await loadTrades();
}

// 내 포트폴리오 조회
async function loadPortfolio() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        window.location.href = '/login';
        return;
    }

    const response = await fetch('/api/portfolio', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        renderEmptyPortfolio();
        return;
    }

    const portfolio = await response.json();

    renderPortfolioSummary(portfolio);
    renderHoldings(portfolio.holdings || []);
}

// 내 거래내역 조회
async function loadTrades() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        window.location.href = '/login';
        return;
    }

    const response = await fetch('/api/trades', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        renderEmptyTrades();
        return;
    }

    const trades = await response.json();
    renderTrades(trades || []);
}

// 포트폴리오 요약 정보 렌더링
function renderPortfolioSummary(portfolio) {
    const holdings = portfolio.holdings || [];
    const cashBalance = Number(portfolio.cashBalance || 0);
    const totalEvaluation = holdings.reduce((sum, item) => sum + Number(item.evaluationAmount || 0), 0);
    const totalPurchase = holdings.reduce((sum, item) => {
        return sum + Number(item.averagePrice || 0) * Number(item.quantity || 0);
    }, 0);
    const totalProfitLoss = holdings.reduce((sum, item) => sum + Number(item.profitLoss || 0), 0);
    const totalAsset = cashBalance + totalEvaluation;
    const totalProfitRate = totalPurchase === 0 ? 0 : totalProfitLoss / totalPurchase * 100;

    setPortfolioText('cash-balance', `${portfolioFormatNumber(cashBalance)}원`);
    setPortfolioText('total-asset', `${portfolioFormatNumber(totalAsset)}원`);
    setPortfolioText('total-evaluation', `${portfolioFormatNumber(totalEvaluation)}원`);
    setPortfolioText('total-profit-loss', `${portfolioFormatSignedNumber(totalProfitLoss)}원`);
    setPortfolioText('total-profit-rate', `${portfolioFormatSignedNumber(totalProfitRate.toFixed(2))}%`);

    setProfitClass('total-profit-loss', totalProfitLoss);
    setProfitClass('total-profit-rate', totalProfitRate);
}

// 보유 종목 목록 렌더링
function renderHoldings(holdings) {
    const wrap = document.getElementById('holding-table-wrap');

    if (!wrap) {
        return;
    }

    if (!holdings || holdings.length === 0) {
        renderEmptyPortfolio();
        return;
    }

    wrap.innerHTML = `
        <table class="holding-table">
            <thead>
            <tr>
                <th>종목</th>
                <th>보유수량</th>
                <th>평균단가</th>
                <th>현재가</th>
                <th>평가금액</th>
                <th>손익</th>
                <th>수익률</th>
                <th>주문</th>
            </tr>
            </thead>
            <tbody>
            ${holdings.map((holding) => `
                <tr>
                    <td>
                        <div class="holding-name">
                            <strong>${portfolioEscapeHtml(holding.stockName)}</strong>
                            <span>${portfolioEscapeHtml(holding.symbol)}</span>
                        </div>
                    </td>
                    <td>${portfolioFormatNumber(holding.quantity)}주</td>
                    <td>${portfolioFormatNumber(holding.averagePrice)}원</td>
                    <td>${portfolioFormatNumber(holding.currentPrice)}원</td>
                    <td>${portfolioFormatNumber(holding.evaluationAmount)}원</td>
                    <td class="${Number(holding.profitLoss) >= 0 ? 'up' : 'down'}">
                        ${portfolioFormatSignedNumber(holding.profitLoss)}원
                    </td>
                    <td class="${Number(holding.profitRate) >= 0 ? 'up' : 'down'}">
                        ${portfolioFormatSignedNumber(holding.profitRate)}%
                    </td>
                    <td>
                        <div class="holding-actions">
                            <button
                                type="button"
                                class="portfolio-order-btn buy"
                                data-symbol="${portfolioEscapeHtml(holding.symbol)}"
                                data-stock-name="${portfolioEscapeHtml(holding.stockName)}"
                                data-order-type="BUY"
                            >
                                매수
                            </button>
                            <button
                                type="button"
                                class="portfolio-order-btn sell"
                                data-symbol="${portfolioEscapeHtml(holding.symbol)}"
                                data-stock-name="${portfolioEscapeHtml(holding.stockName)}"
                                data-order-type="SELL"
                            >
                                매도
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('')}
            </tbody>
        </table>
    `;

    bindPortfolioOrderButtons();
}

// 거래내역 목록 렌더링
function renderTrades(trades) {
    const wrap = document.getElementById('trade-table-wrap');

    if (!wrap) {
        return;
    }

    if (!trades || trades.length === 0) {
        renderEmptyTrades();
        return;
    }

    wrap.innerHTML = `
        <table class="trade-table">
            <thead>
            <tr>
                <th>종목</th>
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
                        <div class="holding-name">
                            <strong>${portfolioEscapeHtml(trade.stockName)}</strong>
                            <span>${portfolioEscapeHtml(trade.symbol)}</span>
                        </div>
                    </td>
                    <td>
                        <span class="trade-type ${trade.orderType === 'BUY' ? 'buy' : 'sell'}">
                            ${trade.orderType === 'BUY' ? '매수' : '매도'}
                        </span>
                    </td>
                    <td>${portfolioFormatNumber(trade.quantity)}주</td>
                    <td>${portfolioFormatNumber(trade.price)}원</td>
                    <td>${portfolioFormatNumber(trade.totalAmount)}원</td>
                    <td>${formatTradeDate(trade.tradedAt)}</td>
                </tr>
            `).join('')}
            </tbody>
        </table>
    `;
}

// 보유 종목이 없을 때 표시
function renderEmptyPortfolio() {
    const wrap = document.getElementById('holding-table-wrap');

    if (!wrap) {
        return;
    }

    wrap.innerHTML = `
        <div class="portfolio-empty">
            <p>보유 종목이 없습니다.</p>
            <span>관심 종목이나 상세 화면에서 매수를 진행해보세요.</span>
        </div>
    `;
}

// 거래내역이 없을 때 표시
function renderEmptyTrades() {
    const wrap = document.getElementById('trade-table-wrap');

    if (!wrap) {
        return;
    }

    wrap.innerHTML = `
        <div class="portfolio-empty">
            <p>거래내역이 없습니다.</p>
            <span>매수 또는 매도를 진행하면 거래내역이 표시됩니다.</span>
        </div>
    `;
}

// 포트폴리오 화면의 매수/매도 버튼 연결
function bindPortfolioOrderButtons() {
    const buttons = document.querySelectorAll('.portfolio-order-btn');

    buttons.forEach((button) => {
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
}

// 주문 성공 후 현재 화면의 포트폴리오 정보 갱신
window.handleOrderSuccess = async function () {
    await loadPortfolioDashboard();
};

function formatTradeDate(value) {
    if (!value) {
        return '-';
    }

    return String(value).replace('T', ' ').substring(0, 16);
}

function setPortfolioText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function setProfitClass(id, value) {
    const element = document.getElementById(id);

    if (!element) {
        return;
    }

    element.classList.remove('up', 'down');
    element.classList.add(Number(value) >= 0 ? 'up' : 'down');
}

function portfolioFormatNumber(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

function portfolioFormatSignedNumber(value) {
    const number = Number(value || 0);
    const sign = number > 0 ? '+' : '';

    return `${sign}${number.toLocaleString('ko-KR')}`;
}

function portfolioEscapeHtml(value) {
    return String(value || '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

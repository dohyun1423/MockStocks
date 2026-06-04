// 개인 포트폴리오 화면에서 현금, 평가금액, 보유 종목을 조회하고 렌더링하는 스크립트

document.addEventListener('DOMContentLoaded', () => {
    loadPortfolio();
});

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

    setText('cash-balance', `${formatNumber(cashBalance)}원`);
    setText('total-asset', `${formatNumber(totalAsset)}원`);
    setText('total-evaluation', `${formatNumber(totalEvaluation)}원`);
    setText('total-profit-loss', `${formatSignedNumber(totalProfitLoss)}원`);
    setText('total-profit-rate', `${formatSignedNumber(totalProfitRate.toFixed(2))}%`);

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
                            <strong>${escapeHtml(holding.stockName)}</strong>
                            <span>${escapeHtml(holding.symbol)}</span>
                        </div>
                    </td>
                    <td>${formatNumber(holding.quantity)}주</td>
                    <td>${formatNumber(holding.averagePrice)}원</td>
                    <td>${formatNumber(holding.currentPrice)}원</td>
                    <td>${formatNumber(holding.evaluationAmount)}원</td>
                    <td class="${Number(holding.profitLoss) >= 0 ? 'up' : 'down'}">
                        ${formatSignedNumber(holding.profitLoss)}원
                    </td>
                    <td class="${Number(holding.profitRate) >= 0 ? 'up' : 'down'}">
                        ${formatSignedNumber(holding.profitRate)}%
                    </td>
                    <td>
                        <div class="holding-actions">
                            <button
                                type="button"
                                class="portfolio-order-btn buy"
                                data-symbol="${escapeHtml(holding.symbol)}"
                                data-stock-name="${escapeHtml(holding.stockName)}"
                                data-order-type="BUY"
                            >
                                매수
                            </button>
                            <button
                                type="button"
                                class="portfolio-order-btn sell"
                                data-symbol="${escapeHtml(holding.symbol)}"
                                data-stock-name="${escapeHtml(holding.stockName)}"
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

// 주문 성공 후 포트폴리오 갱신
window.handleOrderSuccess = async function () {
    await loadPortfolio();
};

function setText(id, value) {
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

function formatNumber(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

function formatSignedNumber(value) {
    const number = Number(value || 0);
    const sign = number > 0 ? '+' : '';

    return `${sign}${number.toLocaleString('ko-KR')}`;
}

function escapeHtml(value) {
    return String(value || '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
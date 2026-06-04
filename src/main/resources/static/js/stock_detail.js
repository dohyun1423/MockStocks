// 주식 상세 화면의 탭, 관심종목, 현재가 정보를 처리하는 스크립트

let currentStock = null;

document.addEventListener('DOMContentLoaded', () => {
    bindStockTabs();
    bindFavoriteButton();
    bindDetailOrderButtons();
    initStockDetail();
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

// 상세 페이지 초기 데이터 조회
async function initStockDetail() {
    const keyword = getTitleStockName();

    if (!keyword) {
        return;
    }

    currentStock = await fetchStockDetail(keyword);

    if (!currentStock) {
        await loadFavoriteStatus([keyword]);
        return;
    }

    setStockTitle(currentStock.name);

    const quote = await fetchStockQuote(currentStock.symbol);

    if (quote) {
        renderStockQuote(quote);
    }

    await loadFavoriteStatus([currentStock.name, keyword]);
}

// 상세 화면 탭 전환 처리
function bindStockTabs() {
    const tabButtons = document.querySelectorAll('.stock-tabs button');
    const tabPanels = document.querySelectorAll('.tab-panel');

    tabButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const target = button.dataset.tab;

            tabButtons.forEach((item) => item.classList.remove('active'));
            tabPanels.forEach((panel) => panel.classList.remove('active'));

            button.classList.add('active');

            const targetPanel = document.getElementById(`tab-${target}`);

            if (targetPanel) {
                targetPanel.classList.add('active');
            }
        });
    });
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
async function loadFavoriteStatus(stockNames) {
    const favoriteButton = document.getElementById('favorite-btn');
    const accessToken = localStorage.getItem('accessToken');

    if (!favoriteButton || !accessToken) {
        return;
    }

    const targetNames = stockNames
        .map(normalizeStockName)
        .filter((name) => name.length > 0);

    if (targetNames.length === 0) {
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

    const exists = watchlists.some((item) => {
        const itemStockName = normalizeStockName(item.stockName || item.stock_name);
        return targetNames.includes(itemStockName);
    });

    favoriteButton.classList.toggle('active', exists);
}

// 관심종목 DB 저장
async function addWatchlist(stockName) {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
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

    if (!accessToken) {
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

// 종목명으로 DB 상세 정보 조회
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

// 종목코드로 더미 quote 현재가 조회
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

// 특정 요소의 텍스트 변경
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

// 관심종목 비교를 위해 공백을 제거하고 영어는 대문자로 변환
function normalizeStockName(value) {
    return String(value || '')
        .replace(/\s+/g, '')
        .toUpperCase();
}

// 숫자를 한국어 콤마 포맷으로 변환
function formatNumber(value) {
    if (value === null || value === undefined || value === '') {
        return '-';
    }

    return Number(value).toLocaleString('ko-KR');
}

// 양수에는 +를 붙여서 표시
function formatSignedNumber(value) {
    if (value === null || value === undefined || value === '') {
        return '-';
    }

    const number = Number(value);
    const sign = number > 0 ? '+' : '';

    return `${sign}${number.toLocaleString('ko-KR')}`;
}
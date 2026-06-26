let orderState = {
    symbol: '',
    stockName: '',
    orderType: 'BUY',
    currentPrice: 0,
    cashBalance: 0,
    holdingQuantity: 0,
    marketSession: null,
    immediateExecution: false,
    reservationAvailable: false
};

// 주문 모달 열기
async function openOrderModal(symbol, stockName, orderType = 'BUY') {
    orderState.symbol = symbol;
    orderState.stockName = stockName;
    orderState.orderType = orderType;

    setOrderMessage('');
    setOrderText('order-stock-name', stockName || symbol || '-');
    setOrderQuantity(1);

    const overlay = document.getElementById('order-modal-overlay');

    if (overlay) {
        overlay.classList.add('active');
    }

    setOrderType(orderType);
    await loadOrderData();
    updateOrderTotalAmount();
}

// 주문 모달 닫기
function closeOrderModal() {
    const overlay = document.getElementById('order-modal-overlay');

    if (overlay) {
        overlay.classList.remove('active');
    }
}

// 매수/매도 타입 변경
function setOrderType(orderType) {
    orderState.orderType = orderType;

    const buyTab = document.getElementById('order-buy-tab');
    const sellTab = document.getElementById('order-sell-tab');
    const submitButton = document.getElementById('order-submit-btn');

    buyTab?.classList.toggle('active', orderType === 'BUY');
    buyTab?.classList.toggle('buy', orderType === 'BUY');

    sellTab?.classList.toggle('active', orderType === 'SELL');
    sellTab?.classList.toggle('sell', orderType === 'SELL');

    if (submitButton) {
        submitButton.textContent = orderType === 'BUY' ? '매수하기' : '매도하기';
        submitButton.classList.toggle('sell', orderType === 'SELL');
    }

    updateOrderTotalAmount();
}

// 주문에 필요한 현재가, 현금, 보유수량 조회
async function loadOrderData() {
    const [quote, portfolio, session] = await Promise.all([
        fetchOrderQuote(orderState.symbol),
        fetchOrderPortfolio(),
        fetchOrderSession()
    ]);

    orderState.currentPrice = Number(quote?.currentPrice || 0);
    orderState.cashBalance = Number(portfolio?.cashBalance || 0);
    orderState.marketSession = session?.marketSession || null;
    orderState.immediateExecution = !!session?.immediateExecution;
    orderState.reservationAvailable = !!session?.reservationAvailable;

    const holding = portfolio?.holdings?.find((item) => item.symbol === orderState.symbol);
    orderState.holdingQuantity = Number(holding?.quantity || 0);

    setOrderText('order-current-price', `${formatOrderNumber(orderState.currentPrice)}원`);
    setOrderText('order-cash-balance', `${formatOrderNumber(orderState.cashBalance)}원`);
    setOrderText('order-holding-quantity', `${formatOrderNumber(orderState.holdingQuantity)}주`);
    setOrderText('order-market-session', session?.displayName || '-');
    setOrderText('order-market-session-message', session?.message || '-');
}

// 주문 세션 조회
async function fetchOrderSession() {
    const response = await authFetch('/api/orders/session');

    if (!response || !response.ok) {
        return null;
    }

    return await response.json();
}

// 현재가 조회
async function fetchOrderQuote(symbol) {
    if (!symbol) {
        return null;
    }

    const response = await authFetch(`/api/stocks/${encodeURIComponent(symbol)}/quote`);

    if (!response || !response.ok) {
        return null;
    }

    return await response.json();
}

// 내 포트폴리오 조회
async function fetchOrderPortfolio() {
    const response = await authFetch('/api/portfolio');

    if (!response || !response.ok) {
        return null;
    }

    return await response.json();
}

// 주문 제출
async function submitOrder() {
    const quantity = getOrderQuantity();

    if (quantity < 1) {
        setOrderMessage('수량은 1주 이상이어야 합니다.', 'error');
        return;
    }

    if (orderState.orderType === 'BUY' && orderState.currentPrice * quantity > orderState.cashBalance) {
        setOrderMessage('보유 현금이 부족합니다.', 'error');
        return;
    }

    if (orderState.orderType === 'SELL' && quantity > orderState.holdingQuantity) {
        setOrderMessage('보유 수량이 부족합니다.', 'error');
        return;
    }

    const result = await requestOrder(quantity);

    if (!result) {
        return;
    }

    setOrderMessage(result.message || '주문이 완료되었습니다.', 'success');
    await loadOrderData();
    updateOrderTotalAmount();

    if (typeof window.handleOrderSuccess === 'function') {
        await window.handleOrderSuccess();
    }

    setTimeout(() => {
        closeOrderModal();
    }, 400);
}

// 매수 또는 매도 API 요청
async function requestOrder(quantity) {
    const endpoint = orderState.orderType === 'BUY' ? '/api/orders/buy' : '/api/orders/sell';

    const response = await authFetch(endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            symbol: orderState.symbol,
            quantity: quantity,
            limitPrice: orderState.currentPrice
        })
    });

    if (!response) {
        return null;
    }

    if (!response.ok) {
        const error = await response.json().catch(() => null);
        setOrderMessage(error?.message || '주문 처리에 실패했습니다.', 'error');
        return null;
    }

    return await response.json();
}

// 수량 변경 시 예상 주문금액 갱신
document.addEventListener('DOMContentLoaded', () => {
    const quantityInput = document.getElementById('order-quantity-input');

    if (quantityInput) {
        quantityInput.addEventListener('input', updateOrderTotalAmount);
    }

    const overlay = document.getElementById('order-modal-overlay');

    if (overlay) {
        overlay.addEventListener('click', (event) => {
            if (event.target === overlay) {
                closeOrderModal();
            }
        });
    }
});

function updateOrderTotalAmount() {
    const quantity = getOrderQuantity();
    const totalAmount = orderState.currentPrice * quantity;

    setOrderText('order-total-amount', `${formatOrderNumber(totalAmount)}원`);
}

function getOrderQuantity() {
    const quantityInput = document.getElementById('order-quantity-input');
    return Number(quantityInput?.value || 0);
}

function setOrderQuantity(quantity) {
    const quantityInput = document.getElementById('order-quantity-input');

    if (quantityInput) {
        quantityInput.value = quantity;
    }
}

function setOrderText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function setOrderMessage(message, type = '') {
    const messageElement = document.getElementById('order-message');

    if (!messageElement) {
        return;
    }

    messageElement.textContent = message;
    messageElement.classList.remove('error', 'success');

    if (type) {
        messageElement.classList.add(type);
    }
}

function formatOrderNumber(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

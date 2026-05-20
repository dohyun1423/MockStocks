document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
    bindStockSearch();
    bindStockTabs();
    bindFavoriteButton();
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

function bindFavoriteButton() {
    const favoriteButton = document.getElementById('favorite-btn');
    const stockTitle = document.querySelector('.stock-title');

    if (!favoriteButton || !stockTitle) {
        return;
    }

    favoriteButton.addEventListener('click', async () => {
        const stockName = stockTitle.textContent.trim();
        const accessToken = localStorage.getItem('accessToken');

        if (!stockName || !accessToken) {
            return;
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

        if (!response.ok) {
            console.error('관심종목 등록 실패');
            return;
        }

        favoriteButton.classList.add('active');
    });
}

function handleLogout() {
    localStorage.removeItem('accessToken');
    window.location.href = '/login';
}
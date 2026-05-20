// ── Ticker ──
const tickers = [
    { sym: 'SAMSUNG', price: '73,400', change: '+1.24%', up: true },
    { sym: 'KAKAO', price: '51,200', change: '-0.87%', up: false },
    { sym: 'NAVER', price: '219,500', change: '+2.10%', up: true },
    { sym: 'HYUNDAI', price: '218,000', change: '+0.46%', up: true },
    { sym: 'LG ENERGY', price: '382,000', change: '-1.55%', up: false },
    { sym: 'SK HYNIX', price: '188,500', change: '+3.22%', up: true },
    { sym: 'POSCO', price: '412,000', change: '-0.24%', up: false },
    { sym: 'CELTRION', price: '172,000', change: '+1.88%', up: true },
    { sym: 'LOTTE', price: '23,850', change: '-0.62%', up: false },
    { sym: 'KIA', price: '115,200', change: '+0.35%', up: true },
];

function buildTicker() {
    const track = document.getElementById('tickerTrack');
    if (!track) return; // 해당 요소가 있는 페이지에서만 실행되도록 방어 코드 추가
    const doubled = [...tickers, ...tickers];
    track.innerHTML = doubled.map(t => `
    <div class="ticker-item">
      <span class="sym">${t.sym}</span>
      <span class="price">${t.price}</span>
      <span class="${t.up ? 'up' : 'down'}">${t.up ? '▲' : '▼'} ${t.change}</span>
    </div>
  `).join('');
}

// ── Password Toggle 공통 함수 ──
function togglePw(id, btn) {
    const input = document.getElementById(id);
    const isText = input.type === 'text';
    input.type = isText ? 'password' : 'text';
    btn.innerHTML = isText
        ? `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>`
        : `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>`;
}

// 초기화 실행
document.addEventListener('DOMContentLoaded', () => {
    buildTicker();
});
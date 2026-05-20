// 스피너 애니메이션용 스타일 주입
const style = document.createElement('style');
style.textContent = '@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }';
document.head.appendChild(style);

function handleLogin() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    let valid = true;

    const emailField = document.getElementById('field-email');
    const pwField = document.getElementById('field-password');

    emailField.classList.remove('error');
    pwField.classList.remove('error');

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        emailField.classList.add('error');
        valid = false;
    }

    if (!password) {
        pwField.classList.add('error');
        valid = false;
    }

    if (valid) {
        const btn = document.querySelector('.btn-submit');
        btn.innerHTML = `
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="animation:spin 0.8s linear infinite">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4"/>
          </svg>
          SIGNING IN...
        `;
        btn.style.opacity = '0.7';
        btn.style.cursor = 'not-allowed';

        setTimeout(() => {
            btn.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20,6 9,17 4,12"/></svg> SUCCESS`;
            btn.style.background = '#00c48c';
            btn.style.opacity = '1';
            window.location.href = '/main';
        }, 1500);
    }
}
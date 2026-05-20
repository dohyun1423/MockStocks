const style = document.createElement('style');
style.textContent = '@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }';
document.head.appendChild(style);

const defaultButtonHtml = `
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
    <polyline points="9,18 15,12 9,6"/>
  </svg>
  SIGN IN
`;

function showLoginError(message) {
    const loginError = document.getElementById('login-error');

    if (!loginError) {
        return;
    }

    loginError.textContent = message;
    loginError.style.display = 'block';
}

function hideLoginError() {
    const loginError = document.getElementById('login-error');

    if (!loginError) {
        return;
    }

    loginError.textContent = '';
    loginError.style.display = 'none';
}

function setLoading(button, loading) {
    button.disabled = loading;
    button.style.opacity = loading ? '0.7' : '1';
    button.style.cursor = loading ? 'not-allowed' : 'pointer';
    button.style.background = '';

    if (loading) {
        button.innerHTML = `
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="animation:spin 0.8s linear infinite">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4"/>
          </svg>
          SIGNING IN...
        `;
        return;
    }

    button.innerHTML = defaultButtonHtml;
}

async function handleLogin() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const emailField = document.getElementById('field-email');
    const pwField = document.getElementById('field-password');
    const btn = document.querySelector('.btn-submit');
    let valid = true;

    hideLoginError();
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

    if (!valid) {
        return;
    }

    setLoading(btn, true);

    try {
        const response = await fetch('/api/users/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (!response.ok) {
            showLoginError(data.message || '로그인에 실패했습니다.');
            return;
        }

        localStorage.setItem('accessToken', data.token);
        window.location.href = '/main';
    } catch (error) {
        console.error(error);
        showLoginError('서버 오류가 발생했습니다.');
    } finally {
        setLoading(btn, false);
    }
}

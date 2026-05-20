// 스피너 애니메이션용 스타일 주입
const style = document.createElement('style');
style.textContent = '@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }';
document.head.appendChild(style);

// ── Password Strength ──
function checkStrength(val) {
    const bars = [1,2,3,4].map(i => document.getElementById('bar' + i));
    const label = document.getElementById('pwLabel');
    bars.forEach(b => { b.className = 'pw-bar'; });

    if (!val) { label.textContent = '비밀번호를 입력해주세요'; label.style.color = ''; return; }

    let score = 0;
    if (val.length >= 8) score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    const levels = [
        { cls: 'weak',   text: '취약한 비밀번호', color: '#ff4560' },
        { cls: 'weak',   text: '취약한 비밀번호', color: '#ff4560' },
        { cls: 'medium', text: '보통 비밀번호',   color: '#f5c518' },
        { cls: 'strong', text: '강력한 비밀번호', color: '#00e5a0' },
        { cls: 'strong', text: '강력한 비밀번호', color: '#00e5a0' },
    ];

    const lv = levels[score];
    for (let i = 0; i < score; i++) bars[i].classList.add(lv.cls);
    label.textContent = lv.text;
    label.style.color = lv.color;
}

// ── Validation ──
async function handleSignup() {

    const email = document.getElementById('email').value.trim();
    const nickname = document.getElementById('nickname').value.trim();
    const password = document.getElementById('password').value;
    const confirm = document.getElementById('confirm').value;

    ['field-email','field-nickname','field-password','field-confirm']
        .forEach(id => {
            document.getElementById(id).classList.remove('error');
        });

    let valid = true;

    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        document.getElementById('field-email').classList.add('error');
        valid = false;
    }

    if (!nickname || nickname.length < 2) {
        document.getElementById('field-nickname').classList.add('error');
        valid = false;
    }

    if (!password || password.length < 8) {
        document.getElementById('field-password').classList.add('error');
        valid = false;
    }

    if (password !== confirm) {
        document.getElementById('field-confirm').classList.add('error');
        valid = false;
    }

    if (!valid) {
        return;
    }

    try {

        const response = await fetch('/api/users/signup', {

            method: 'POST',

            headers: {
                'Content-Type': 'application/json'
            },

            body: JSON.stringify({
                email: email,
                password: password,
                nickname: nickname
            })
        });

        if (!response.ok) {

            const errorData = await response.json();

            alert(errorData.message);

            return;
        }

        alert('회원가입 성공!');

        window.location.href = '/login';

    } catch (error) {

        console.error(error);

        alert('서버 오류 발생');
    }
}
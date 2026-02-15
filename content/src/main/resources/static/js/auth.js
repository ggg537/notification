async function login() {
    var email = document.getElementById('email').value;
    var password = document.getElementById('password').value;
    var errorEl = document.getElementById('errorMsg');
    var infoEl = document.getElementById('infoMsg');
    errorEl.style.display = 'none';
    if (infoEl) infoEl.style.display = 'none';

    try {
        var res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: password })
        });
        if (res.ok) {
            var data = await res.json();
            if (data.emailVerificationRequired && infoEl) {
                infoEl.textContent = '이메일을 인증해주세요. 받은편지함에서 인증 링크를 확인하세요.';
                infoEl.style.display = 'block';
                setTimeout(function() { window.location.href = '/'; }, 3000);
            } else {
                window.location.href = '/';
            }
        } else {
            var data = await res.json().catch(function() { return null; });
            errorEl.textContent = (data && data.message) || '잘못된 이메일 또는 비밀번호입니다';
            errorEl.style.display = 'block';
        }
    } catch (e) {
        errorEl.textContent = '서버 오류';
        errorEl.style.display = 'block';
    }
}

async function signup() {
    var name = document.getElementById('name').value;
    var email = document.getElementById('email').value;
    var password = document.getElementById('password').value;
    var errorEl = document.getElementById('errorMsg');
    var successEl = document.getElementById('successMsg');
    errorEl.style.display = 'none';
    if (successEl) successEl.style.display = 'none';

    if (!name || !email || !password) {
        errorEl.textContent = '모든 필드를 입력해주세요';
        errorEl.style.display = 'block';
        return;
    }

    try {
        var res = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: name, email: email, password: password })
        });
        if (res.ok) {
            if (successEl) {
                successEl.textContent = '계정이 생성되었습니다! 이메일을 확인하여 계정을 인증해주세요.';
                successEl.style.display = 'block';
            }
            setTimeout(function() { window.location.href = '/login'; }, 2000);
        } else {
            var data = await res.json().catch(function() { return null; });
            errorEl.textContent = (data && data.message) || '가입 실패';
            errorEl.style.display = 'block';
        }
    } catch (e) {
        errorEl.textContent = '서버 오류';
        errorEl.style.display = 'block';
    }
}

/* ========== 소셜 로그인 (OAuth) ========== */
var oauthConfig = null;

async function loadOAuthConfig() {
    try {
        var res = await fetch('/api/auth/oauth/config');
        if (res.ok) {
            oauthConfig = await res.json();
        }
    } catch (e) {}
}

function loginWithGoogle() {
    if (!oauthConfig || !oauthConfig.googleClientId) {
        alert('Google OAuth가 설정되어 있지 않습니다.');
        return;
    }
    var redirectUri = encodeURIComponent(window.location.origin + '/oauth/callback/google');
    var scope = encodeURIComponent('openid email profile');
    window.location.href = 'https://accounts.google.com/o/oauth2/v2/auth?client_id=' + oauthConfig.googleClientId +
        '&redirect_uri=' + redirectUri + '&response_type=code&scope=' + scope;
}

function loginWithKakao() {
    if (!oauthConfig || !oauthConfig.kakaoClientId) {
        alert('카카오 OAuth가 설정되어 있지 않습니다.');
        return;
    }
    var redirectUri = encodeURIComponent(window.location.origin + '/oauth/callback/kakao');
    window.location.href = 'https://kauth.kakao.com/oauth/authorize?client_id=' + oauthConfig.kakaoClientId +
        '&redirect_uri=' + redirectUri + '&response_type=code';
}

document.addEventListener('DOMContentLoaded', function() {
    loadOAuthConfig();

    var passwordEl = document.getElementById('password');
    if (passwordEl) {
        passwordEl.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                if (document.getElementById('name') === null) {
                    login();
                } else {
                    signup();
                }
            }
        });
    }
});

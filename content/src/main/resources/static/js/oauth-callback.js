(async function() {
    var params = new URLSearchParams(window.location.search);
    var code = params.get('code');
    var error = params.get('error');
    var errorEl = document.getElementById('errorMsg');
    var statusTitle = document.getElementById('statusTitle');
    var statusMsg = document.getElementById('statusMsg');

    // 경로에서 제공자 판별: /oauth/callback/google 또는 /oauth/callback/kakao
    var pathParts = window.location.pathname.split('/');
    var provider = pathParts[pathParts.length - 1];

    if (error || !code) {
        statusTitle.textContent = '로그인 실패';
        errorEl.textContent = error || '인증 코드를 받지 못했습니다';
        errorEl.style.display = 'block';
        statusMsg.textContent = '';
        return;
    }

    try {
        var redirectUri = window.location.origin + '/oauth/callback/' + provider;
        var res = await fetch('/api/auth/oauth/' + provider, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code, redirectUri: redirectUri })
        });

        if (res.ok) {
            statusTitle.textContent = '성공!';
            statusMsg.textContent = '이동 중...';
            window.location.href = '/';
        } else {
            var data = await res.json().catch(function() { return null; });
            statusTitle.textContent = '로그인 실패';
            errorEl.textContent = (data && data.message) || '소셜 로그인 실패';
            errorEl.style.display = 'block';
            statusMsg.innerHTML = '<a href="/login">로그인으로 돌아가기</a>';
        }
    } catch (e) {
        statusTitle.textContent = '로그인 실패';
        errorEl.textContent = '서버 오류';
        errorEl.style.display = 'block';
        statusMsg.innerHTML = '<a href="/login">로그인으로 돌아가기</a>';
    }
})();

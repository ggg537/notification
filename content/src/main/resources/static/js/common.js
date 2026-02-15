/* ========== 전역 상태 ========== */
var currentUser = null;
var isRefreshing = false;
var refreshPromise = null;

/* ========== API 래퍼 ========== */
async function api(url, options) {
    if (!options) options = {};
    var defaults = {
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin'
    };
    var config = Object.assign({}, defaults, options);
    if (options.headers) {
        config.headers = Object.assign({}, defaults.headers, options.headers);
    }
    // FormData인 경우 Content-Type 설정하지 않음
    if (options.body instanceof FormData) {
        delete config.headers['Content-Type'];
    }
    var response = await fetch(url, config);
    if (response.status === 401 && !url.includes('/api/auth/refresh') && !url.includes('/api/auth/login')) {
        // 토큰 갱신 시도
        var refreshed = await tryRefreshToken();
        if (refreshed) {
            // 원래 요청 재시도
            response = await fetch(url, config);
        } else {
            window.location.href = '/login';
            return null;
        }
    }
    return response;
}

async function tryRefreshToken() {
    if (isRefreshing) {
        return refreshPromise;
    }
    isRefreshing = true;
    refreshPromise = (async function() {
        try {
            var res = await fetch('/api/auth/refresh', {
                method: 'POST',
                credentials: 'same-origin'
            });
            return res.ok;
        } catch (e) {
            return false;
        } finally {
            isRefreshing = false;
            refreshPromise = null;
        }
    })();
    return refreshPromise;
}

/* ========== 유틸리티 ========== */
function timeAgo(dateStr) {
    var now = new Date();
    var date = new Date(dateStr);
    var diff = Math.floor((now - date) / 1000);
    if (diff < 60) return diff + 's';
    if (diff < 3600) return Math.floor(diff / 60) + 'm';
    if (diff < 86400) return Math.floor(diff / 3600) + 'h';
    var days = Math.floor(diff / 86400);
    if (days < 7) return days + 'd';
    return date.toLocaleDateString();
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function avatarUrl(url) {
    return url || 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 36 36"><rect width="36" height="36" rx="10" fill="%23e8e8e4"/><text x="18" y="23" text-anchor="middle" fill="%23a8a8a4" font-size="16">?</text></svg>');
}

function renderPostContent(text) {
    if (!text) return '';
    var escaped = escapeHtml(text);
    // #해시태그를 링크로 변환
    escaped = escaped.replace(/#(\w+)/g, '<a href="/search?q=%23$1" class="hashtag-link">#$1</a>');
    // @멘션을 프로필 링크로 변환
    escaped = escaped.replace(/@(\w+)/g, '<a href="/search?q=%40$1" class="mention-link">@$1</a>');
    return escaped;
}

/* ========== 인증 ========== */
async function loadCurrentUser() {
    try {
        var res = await api('/api/auth/me');
        if (res && res.ok) {
            currentUser = await res.json();
        }
    } catch (e) {}
    return currentUser;
}

async function logout() {
    await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' });
    window.location.href = '/login';
}

/* ========== 테마 ========== */
function getTheme() {
    return localStorage.getItem('theme') || 'light';
}

function setTheme(theme) {
    localStorage.setItem('theme', theme);
    document.documentElement.setAttribute('data-theme', theme);
    var toggle = document.querySelector('.theme-toggle');
    if (toggle) toggle.textContent = theme === 'dark' ? '☀' : '☾';
}

function toggleTheme() {
    setTheme(getTheme() === 'dark' ? 'light' : 'dark');
}

/* ========== 플로팅 네비게이션 ========== */
function renderFloatingNav() {
    var nav = document.getElementById('floating-nav');
    if (!nav) return;

    var path = window.location.pathname;
    var isAuth = path === '/login' || path === '/signup' || path === '/forgot-password' || path.startsWith('/reset-password') || path.startsWith('/oauth/callback');

    if (!currentUser) {
        // 비로그인 사용자에게 로그인/회원가입 네비 표시
        if (isAuth) {
            nav.innerHTML = '';
            return;
        }
        nav.innerHTML =
            '<nav class="floating-nav">' +
                '<a href="/login" class="nav-item">로그인</a>' +
                '<a href="/signup" class="nav-item">회원가입</a>' +
                '<button class="theme-toggle" onclick="toggleTheme()">' + (getTheme() === 'dark' ? '☀' : '☾') + '</button>' +
            '</nav>';
        return;
    }

    var feedActive = path === '/' ? ' active' : '';
    var searchActive = path === '/search' ? ' active' : '';
    var notifActive = path === '/notifications' ? ' active' : '';
    var profileActive = path.startsWith('/profile/') ? ' active' : '';

    nav.innerHTML =
        '<nav class="floating-nav">' +
            '<a href="/" class="nav-item' + feedActive + '">피드</a>' +
            '<a href="/search" class="nav-item' + searchActive + '">검색</a>' +
            '<a href="/notifications" class="nav-item' + notifActive + '">알림<span id="notifDot" class="notif-dot"></span></a>' +
            '<a href="/profile/' + currentUser.id + '" class="nav-item' + profileActive + '">프로필</a>' +
            '<button class="nav-item" onclick="logout()">로그아웃</button>' +
            '<button class="theme-toggle" onclick="toggleTheme()">' + (getTheme() === 'dark' ? '☀' : '☾') + '</button>' +
        '</nav>';
}

/* ========== 알림 뱃지 폴링 ========== */
function startNotifBadgePolling() {
    async function check() {
        if (!currentUser) return;
        try {
            var res = await fetch('/v1/user-notifications/' + currentUser.id + '/new');
            if (res.ok) {
                var data = await res.json();
                var dot = document.getElementById('notifDot');
                if (dot) {
                    dot.classList.toggle('show', data.hasNew);
                }
            }
        } catch (e) {}
    }
    check();
    setInterval(check, 5000);
}

/* ========== 게시글 카드 렌더러 (공용) ========== */
function renderPostCard(post, opts) {
    opts = opts || {};
    var card = document.createElement('div');
    card.className = 'post-card';
    card.dataset.postId = post.id;

    var visibilityBadge = '';
    if (post.visibility === 'FOLLOWERS_ONLY') {
        visibilityBadge = '<span class="post-visibility-badge followers-only">팔로워 전용</span>';
    } else if (post.visibility === 'PRIVATE') {
        visibilityBadge = '<span class="post-visibility-badge private">비공개</span>';
    }

    var imageHtml = '';
    if (post.imageUrl) {
        imageHtml = '<img class="post-image" src="' + escapeHtml(post.imageUrl) + '" alt="">';
    }

    var tagsHtml = '';
    if (post.tags && post.tags.length > 0) {
        tagsHtml = '<div class="post-tags">' + post.tags.map(function(t) {
            return '<a href="/search?q=%23' + encodeURIComponent(t) + '" class="tag-link">#' + escapeHtml(t) + '</a>';
        }).join(' ') + '</div>';
    }

    var likedClass = post.liked ? ' liked' : '';
    var likeIcon = post.liked ? '♥' : '♡';
    var bookmarkedClass = post.bookmarked ? ' bookmarked' : '';
    var bookmarkIcon = post.bookmarked ? '★' : '☆';

    var followBtnHtml = '';
    if (currentUser && !post.isOwnPost) {
        var fClass = post.isFollowing ? 'btn btn-follow btn-xs following' : 'btn btn-follow btn-xs';
        var fText = post.isFollowing ? '팔로잉' : '팔로우';
        followBtnHtml = '<button class="' + fClass + '" data-user-id="' + post.userId + '" onclick="togglePostFollow(this)">' + fText + '</button>';
    }

    card.innerHTML =
        '<div class="post-header">' +
            '<img class="post-avatar" src="' + avatarUrl(post.authorProfileImageUrl) + '" alt="">' +
            '<div class="post-author-info">' +
                '<a href="/profile/' + post.userId + '" class="post-author-name">' + escapeHtml(post.authorName) + '</a>' +
                (post.authorHandle ? '<span class="post-author-handle">@' + escapeHtml(post.authorHandle) + '</span>' : '') +
            '</div>' +
            followBtnHtml +
            visibilityBadge +
            '<span class="post-time">' + timeAgo(post.createdAt) + '</span>' +
        '</div>' +
        '<div class="post-content">' + renderPostContent(post.content) + '</div>' +
        imageHtml +
        tagsHtml +
        '<div class="post-actions">' +
            '<button class="action-btn' + likedClass + '" data-post-id="' + post.id + '" onclick="toggleLike(this)">' +
                '<span class="action-icon">' + likeIcon + '</span> <span class="like-count">' + post.likeCount + '</span>' +
            '</button>' +
            '<button class="action-btn comment-toggle-btn" data-post-id="' + post.id + '" onclick="toggleComments(this)">' +
                '<span class="action-icon">💬</span> <span class="comment-count">' + post.commentCount + '</span>' +
            '</button>' +
            '<button class="action-btn' + bookmarkedClass + '" data-post-id="' + post.id + '" onclick="toggleBookmark(this)">' +
                '<span class="action-icon">' + bookmarkIcon + '</span>' +
            '</button>' +
        '</div>' +
        '<div class="inline-comments" id="comments-' + post.id + '"></div>';

    return card;
}

/* ========== 인라인 댓글 (대댓글 포함) ========== */
async function toggleComments(btn) {
    var postId = btn.dataset.postId;
    var commentsDiv = document.getElementById('comments-' + postId);
    if (!commentsDiv) return;

    if (commentsDiv.classList.contains('open')) {
        commentsDiv.classList.remove('open');
        return;
    }

    commentsDiv.classList.add('open');
    commentsDiv.innerHTML = '<div class="loading-indicator">로딩 중...</div>';

    var res = await api('/api/posts/' + postId + '/comments');
    if (!res || !res.ok) return;
    var comments = await res.json();

    var html = renderCommentForm(postId, null);

    comments.forEach(function(c) {
        html += renderCommentItem(c, postId);
        if (c.replies && c.replies.length > 0) {
            c.replies.forEach(function(r) {
                html += renderCommentItem(r, postId, true);
            });
        }
    });

    commentsDiv.innerHTML = html;
}

function renderCommentForm(postId, parentId) {
    var parentAttr = parentId ? ' data-parent-id="' + parentId + '"' : '';
    return '<div class="comment-form"' + parentAttr + '>' +
        '<img class="comment-avatar" src="' + avatarUrl(currentUser ? currentUser.profileImageUrl : null) + '" alt="">' +
        '<input type="text" placeholder="' + (parentId ? '답글을 입력하세요...' : '댓글을 입력하세요...') + '" onkeypress="if(event.key===\'Enter\')addInlineComment(this,' + postId + ',' + (parentId || 'null') + ')">' +
        '<button class="btn btn-primary btn-sm" onclick="addInlineComment(this.previousElementSibling,' + postId + ',' + (parentId || 'null') + ')">게시</button>' +
    '</div>';
}

function renderCommentItem(c, postId, isReply) {
    var replyClass = isReply ? ' comment-reply' : '';
    var replyBtn = '';
    if (!isReply && c.depth === 0) {
        replyBtn = '<button class="comment-reply-btn" onclick="showReplyForm(this,' + postId + ',' + c.id + ')">답글</button>';
    }
    return '<div class="comment-item' + replyClass + '">' +
        '<img class="comment-avatar" src="' + avatarUrl(c.authorProfileImageUrl) + '" alt="">' +
        '<div class="comment-body">' +
            '<span class="comment-author">' + escapeHtml(c.authorName) + '</span> ' +
            '<span class="comment-text">' + escapeHtml(c.content) + '</span>' +
            '<div class="comment-meta">' +
                '<span class="comment-time">' + timeAgo(c.createdAt) + '</span>' +
                replyBtn +
                (c.isOwner ? '<button class="comment-delete" data-comment-id="' + c.id + '" data-post-id="' + postId + '" onclick="deleteInlineComment(this)">삭제</button>' : '') +
            '</div>' +
        '</div>' +
    '</div>';
}

function showReplyForm(btn, postId, parentId) {
    // 기존 답글 폼 제거
    var existing = btn.closest('.comment-item').parentNode.querySelector('.reply-form-container');
    if (existing) { existing.remove(); return; }
    var container = document.createElement('div');
    container.className = 'reply-form-container comment-reply';
    container.innerHTML = renderCommentForm(postId, parentId);
    btn.closest('.comment-item').insertAdjacentElement('afterend', container);
    container.querySelector('input').focus();
}

async function addInlineComment(input, postId, parentId) {
    var content = input.value.trim();
    if (!content) return;
    var body = { postId: postId, content: content };
    if (parentId) body.parentId = parentId;
    var res = await api('/api/comments', {
        method: 'POST',
        body: JSON.stringify(body)
    });
    if (res && res.ok) {
        input.value = '';
        // 댓글 새로고침
        var btn = document.querySelector('.comment-toggle-btn[data-post-id="' + postId + '"]');
        var commentsDiv = document.getElementById('comments-' + postId);
        if (commentsDiv) commentsDiv.classList.remove('open');
        if (btn) {
            var countEl = btn.querySelector('.comment-count');
            if (countEl) countEl.textContent = parseInt(countEl.textContent) + 1;
            toggleComments(btn);
        }
    }
}

async function deleteInlineComment(el) {
    var commentId = el.dataset.commentId;
    var postId = el.dataset.postId;
    var res = await api('/api/comments/' + commentId, { method: 'DELETE' });
    if (res && res.ok) {
        var btn = document.querySelector('.comment-toggle-btn[data-post-id="' + postId + '"]');
        var commentsDiv = document.getElementById('comments-' + postId);
        if (commentsDiv) commentsDiv.classList.remove('open');
        if (btn) {
            var countEl = btn.querySelector('.comment-count');
            if (countEl) countEl.textContent = Math.max(0, parseInt(countEl.textContent) - 1);
            toggleComments(btn);
        }
    }
}

/* ========== 게시글 카드 팔로우 토글 ========== */
async function togglePostFollow(el) {
    var userId = el.dataset.userId;
    var res = await api('/api/follows/' + userId, { method: 'POST' });
    if (res && res.ok) {
        var data = await res.json();
        var newText = data.following ? '팔로잉' : '팔로우';
        var newClass = data.following ? 'btn btn-follow btn-xs following' : 'btn btn-follow btn-xs';
        // 모든 게시글 카드에서 이 사용자의 팔로우 버튼 동기화
        document.querySelectorAll('.btn-follow[data-user-id="' + userId + '"]').forEach(function(btn) {
            btn.textContent = newText;
            btn.className = newClass;
        });
    }
}

/* ========== 좋아요 토글 (공용) ========== */
async function toggleLike(el) {
    var postId = el.dataset.postId;
    var res = await api('/api/likes/' + postId, { method: 'POST' });
    if (res && res.ok) {
        var data = await res.json();
        el.classList.toggle('liked', data.liked);
        el.querySelector('.action-icon').textContent = data.liked ? '♥' : '♡';
        el.querySelector('.like-count').textContent = data.count;
    }
}

/* ========== 북마크 토글 (공용) ========== */
async function toggleBookmark(el) {
    var postId = el.dataset.postId;
    var res = await api('/api/bookmarks/' + postId, { method: 'POST' });
    if (res && res.ok) {
        var data = await res.json();
        el.classList.toggle('bookmarked', data.bookmarked);
        el.querySelector('.action-icon').textContent = data.bookmarked ? '★' : '☆';
    }
}

/* ========== 초기화 ========== */
document.addEventListener('DOMContentLoaded', function() {
    setTheme(getTheme());
    loadCurrentUser().then(function() {
        renderFloatingNav();
        startNotifBadgePolling();
    });
});

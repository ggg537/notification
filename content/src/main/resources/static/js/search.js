var searchTab = 'posts';
var searchPage = 0;
var searchLoading = false;
var searchHasMore = true;
var lastQuery = '';

function getQueryParam() {
    var params = new URLSearchParams(window.location.search);
    return params.get('q') || '';
}

async function performSearch(query, tab, page, append) {
    if (searchLoading) return;
    if (!query.trim()) return;
    searchLoading = true;

    var loader = document.getElementById('loadingIndicator');
    var emptyState = document.getElementById('emptyState');
    var results = document.getElementById('searchResults');
    if (loader) loader.classList.remove('hidden');
    if (!append) results.innerHTML = '';

    var res = await api('/api/search?q=' + encodeURIComponent(query) + '&type=' + tab + '&page=' + page + '&size=20');
    if (!res || !res.ok) {
        searchLoading = false;
        if (loader) loader.classList.add('hidden');
        return;
    }
    var data = await res.json();

    if (tab === 'posts') {
        if (data.posts.length === 0 && page === 0) {
            emptyState.classList.remove('hidden');
        } else {
            emptyState.classList.add('hidden');
            data.posts.forEach(function(post) {
                results.appendChild(renderPostCard(post));
            });
        }
        searchHasMore = data.hasNext;
    } else if (tab === 'users') {
        if (data.users.length === 0 && page === 0) {
            emptyState.classList.remove('hidden');
        } else {
            emptyState.classList.add('hidden');
            data.users.forEach(function(user) {
                results.appendChild(renderUserResult(user));
            });
        }
        searchHasMore = data.hasNext;
    } else if (tab === 'tags') {
        if (data.tags.length === 0 && page === 0) {
            emptyState.classList.remove('hidden');
        } else {
            emptyState.classList.add('hidden');
            data.tags.forEach(function(tag) {
                results.appendChild(renderTagResult(tag));
            });
        }
        searchHasMore = data.hasNext;
    }

    searchPage = page;
    if (loader) loader.classList.add('hidden');
    searchLoading = false;
}

function renderUserResult(user) {
    var div = document.createElement('div');
    div.className = 'user-result-item';
    var followBtn = '';
    if (currentUser && currentUser.id !== user.id) {
        var btnClass = user.isFollowing ? 'btn btn-follow btn-sm following' : 'btn btn-follow btn-sm';
        var btnText = user.isFollowing ? '팔로잉' : '팔로우';
        followBtn = '<button class="' + btnClass + '" data-user-id="' + user.id + '" onclick="toggleSearchFollow(this)">' + btnText + '</button>';
    }
    div.innerHTML =
        '<a href="/profile/' + user.id + '" class="user-result-info">' +
            '<img class="user-result-avatar" src="' + avatarUrl(user.profileImageUrl) + '" alt="">' +
            '<div>' +
                '<div class="user-result-name">' + escapeHtml(user.name) + '</div>' +
                (user.handle ? '<div class="user-result-handle">@' + escapeHtml(user.handle) + '</div>' : '') +
            '</div>' +
        '</a>' +
        followBtn;
    return div;
}

function renderTagResult(tag) {
    var div = document.createElement('div');
    div.className = 'tag-result-item';
    div.innerHTML =
        '<a href="/search?q=%23' + encodeURIComponent(tag.tag) + '" class="tag-result-link">' +
            '<span class="tag-result-name">#' + escapeHtml(tag.tag) + '</span>' +
            '<span class="tag-result-count">' + tag.postCount + '개 게시글</span>' +
        '</a>';
    return div;
}

async function toggleSearchFollow(el) {
    var userId = el.dataset.userId;
    var res = await api('/api/follows/' + userId, { method: 'POST' });
    if (res && res.ok) {
        var data = await res.json();
        el.textContent = data.following ? '팔로잉' : '팔로우';
        el.className = data.following ? 'btn btn-follow btn-sm following' : 'btn btn-follow btn-sm';
    }
}

async function loadTrending() {
    var res = await api('/api/search/trending');
    if (!res || !res.ok) return;
    var tags = await res.json();
    var container = document.getElementById('trendingTags');
    if (!container) return;
    if (tags.length === 0) {
        container.innerHTML = '<div class="empty-state">아직 트렌딩 태그가 없습니다</div>';
        return;
    }
    container.innerHTML = tags.map(function(tag) {
        return '<a href="/search?q=%23' + encodeURIComponent(tag.tag) + '" class="trending-tag-item">' +
            '<span class="trending-tag-name">#' + escapeHtml(tag.tag) + '</span>' +
            '<span class="trending-tag-count">' + tag.postCount + '</span>' +
        '</a>';
    }).join('');
}

document.addEventListener('DOMContentLoaded', function() {
    var input = document.getElementById('searchInput');
    var q = getQueryParam();
    if (q) {
        input.value = q;
        lastQuery = q;
        // @로 시작하면 사용자 검색
        if (q.startsWith('@')) {
            searchTab = 'users';
            document.querySelectorAll('#searchTabs .tab').forEach(function(t) { t.classList.remove('active'); });
            document.querySelector('#searchTabs .tab[data-tab="users"]').classList.add('active');
        }
        performSearch(q, searchTab, 0, false);
    }

    input.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            var val = input.value.trim();
            if (val) {
                lastQuery = val;
                searchPage = 0;
                searchHasMore = true;
                window.history.replaceState(null, '', '/search?q=' + encodeURIComponent(val));
                performSearch(val, searchTab, 0, false);
            }
        }
    });

    // 탭 전환
    var tabs = document.getElementById('searchTabs');
    if (tabs) {
        tabs.addEventListener('click', function(e) {
            var tab = e.target.closest('.tab');
            if (!tab) return;
            tabs.querySelectorAll('.tab').forEach(function(t) { t.classList.remove('active'); });
            tab.classList.add('active');
            searchTab = tab.dataset.tab;
            searchPage = 0;
            searchHasMore = true;
            if (lastQuery) performSearch(lastQuery, searchTab, 0, false);
        });
    }

    // 무한 스크롤
    var sentinel = document.createElement('div');
    sentinel.style.height = '1px';
    var results = document.getElementById('searchResults');
    if (results) results.parentNode.insertBefore(sentinel, results.nextSibling);
    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting && !searchLoading && searchHasMore && lastQuery) {
            performSearch(lastQuery, searchTab, searchPage + 1, true);
        }
    }, { rootMargin: '200px' });
    observer.observe(sentinel);

    loadTrending();
});

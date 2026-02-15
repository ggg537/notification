var profileUserId = window.location.pathname.split('/').pop();
var currentTab = 'posts';
var currentPage = 0;
var isLoading = false;
var hasMore = true;
var profileData = null;

/* ========== 프로필 로딩 ========== */
async function loadProfile() {
    var res = await api('/api/users/' + profileUserId);
    if (!res || !res.ok) return;
    profileData = await res.json();

    var card = document.getElementById('profileCard');
    var followBtn = '';
    if (!profileData.isOwnProfile) {
        var btnClass = profileData.isFollowing ? 'btn btn-follow following' : 'btn btn-follow';
        var btnText = profileData.isFollowing ? '팔로잉' : '팔로우';
        followBtn = '<button id="followBtn" class="' + btnClass + '" data-user-id="' + profileData.id + '" onclick="toggleFollow(this)">' + btnText + '</button>';
    } else {
        followBtn = '<button class="btn btn-outline" onclick="openEditModal()">프로필 편집</button>';
    }

    card.innerHTML =
        '<div class="profile-card">' +
            '<img class="profile-avatar" src="' + avatarUrl(profileData.profileImageUrl) + '" alt="">' +
            '<div class="profile-name">' + escapeHtml(profileData.name) + '</div>' +
            (profileData.handle ? '<div class="profile-handle">@' + escapeHtml(profileData.handle) + '</div>' : '') +
            (profileData.bio ? '<div class="profile-bio">' + escapeHtml(profileData.bio) + '</div>' : '') +
            '<div class="profile-stats">' +
                '<div class="profile-stat"><div class="stat-value">' + profileData.postCount + '</div><div class="stat-label">게시글</div></div>' +
                '<div class="profile-stat"><div class="stat-value" id="followerCount">' + profileData.followerCount + '</div><div class="stat-label">팔로워</div></div>' +
                '<div class="profile-stat"><div class="stat-value">' + profileData.followingCount + '</div><div class="stat-label">팔로잉</div></div>' +
            '</div>' +
            '<div class="profile-actions">' + followBtn + '</div>' +
        '</div>';

    loadUserPosts(0, false);
}

/* ========== 사용자 게시글 ========== */
async function loadUserPosts(page, append) {
    if (isLoading) return;
    isLoading = true;

    var loader = document.getElementById('loadingIndicator');
    var emptyState = document.getElementById('emptyState');
    if (loader) loader.classList.remove('hidden');

    var res = await api('/api/users/' + profileUserId + '/posts?page=' + page + '&size=20');
    if (!res || !res.ok) { isLoading = false; if (loader) loader.classList.add('hidden'); return; }
    var data = await res.json();

    var postList = document.getElementById('postList');
    if (!append) postList.innerHTML = '';

    if (data.posts.length === 0 && page === 0) {
        emptyState.classList.remove('hidden');
    } else {
        emptyState.classList.add('hidden');
    }

    data.posts.forEach(function(post) {
        postList.appendChild(renderPostCard(post));
    });

    currentPage = data.page;
    hasMore = data.hasNext;

    if (loader) loader.classList.add('hidden');
    isLoading = false;
}

/* ========== 북마크한 게시글 ========== */
async function loadBookmarks() {
    var loader = document.getElementById('loadingIndicator');
    var emptyState = document.getElementById('emptyState');
    var postList = document.getElementById('postList');

    if (loader) loader.classList.remove('hidden');
    postList.innerHTML = '';

    var res = await api('/api/bookmarks');
    if (!res || !res.ok) { if (loader) loader.classList.add('hidden'); return; }
    var posts = await res.json();

    if (posts.length === 0) {
        emptyState.classList.remove('hidden');
    } else {
        emptyState.classList.add('hidden');
    }

    posts.forEach(function(post) {
        postList.appendChild(renderPostCard(post));
    });

    if (loader) loader.classList.add('hidden');
}

/* ========== 팔로우 토글 ========== */
async function toggleFollow(el) {
    var userId = el.dataset.userId;
    var res = await api('/api/follows/' + userId, { method: 'POST' });
    if (res && res.ok) {
        var data = await res.json();
        el.textContent = data.following ? '팔로잉' : '팔로우';
        el.className = data.following ? 'btn btn-follow following' : 'btn btn-follow';
        var countEl = document.getElementById('followerCount');
        var count = parseInt(countEl.textContent);
        countEl.textContent = data.following ? count + 1 : count - 1;
    }
}

/* ========== 프로필 편집 모달 ========== */
function openEditModal() {
    if (!profileData) return;
    document.getElementById('editName').value = profileData.name || '';
    document.getElementById('editHandle').value = profileData.handle || '';
    document.getElementById('editBio').value = profileData.bio || '';
    document.getElementById('editImage').value = '';
    document.getElementById('editOverlay').classList.add('open');
}

function closeEditModal() {
    document.getElementById('editOverlay').classList.remove('open');
}

async function saveProfile() {
    var name = document.getElementById('editName').value.trim();
    var handle = document.getElementById('editHandle').value.trim();
    var bio = document.getElementById('editBio').value.trim();
    var imageFile = document.getElementById('editImage').files[0];

    // 프로필 텍스트 필드 업데이트
    var res = await api('/api/users/' + profileUserId, {
        method: 'PUT',
        body: JSON.stringify({ name: name, bio: bio, handle: handle })
    });

    // 이미지가 선택된 경우 업로드
    if (imageFile) {
        var formData = new FormData();
        formData.append('file', imageFile);
        await api('/api/users/' + profileUserId + '/image', {
            method: 'PUT',
            body: formData
        });
    }

    closeEditModal();
    loadProfile();
}

/* ========== 비밀번호 변경 ========== */
async function changePassword() {
    var currentPw = document.getElementById('currentPassword').value;
    var newPw = document.getElementById('newPassword').value;
    var confirmPw = document.getElementById('confirmNewPassword').value;
    var msgEl = document.getElementById('passwordMsg');
    var errEl = document.getElementById('passwordError');
    msgEl.style.display = 'none';
    errEl.style.display = 'none';

    if (!currentPw || !newPw) {
        errEl.textContent = '모든 필드를 입력해주세요';
        errEl.style.display = 'block';
        return;
    }
    if (newPw.length < 4) {
        errEl.textContent = '새 비밀번호는 최소 4자 이상이어야 합니다';
        errEl.style.display = 'block';
        return;
    }
    if (newPw !== confirmPw) {
        errEl.textContent = '비밀번호가 일치하지 않습니다';
        errEl.style.display = 'block';
        return;
    }

    var res = await api('/api/auth/password', {
        method: 'PUT',
        body: JSON.stringify({ currentPassword: currentPw, newPassword: newPw })
    });
    if (res && res.ok) {
        msgEl.textContent = '비밀번호가 변경되었습니다';
        msgEl.style.display = 'block';
        document.getElementById('currentPassword').value = '';
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmNewPassword').value = '';
    } else {
        var data = res ? await res.json().catch(function() { return null; }) : null;
        errEl.textContent = (data && data.message) || '비밀번호 변경 실패';
        errEl.style.display = 'block';
    }
}

/* ========== 계정 삭제 ========== */
async function deleteAccount() {
    var password = document.getElementById('deletePassword').value;
    var errEl = document.getElementById('deleteError');
    errEl.style.display = 'none';

    if (!confirm('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        return;
    }

    var res = await api('/api/auth/account', {
        method: 'DELETE',
        body: JSON.stringify({ password: password })
    });
    if (res && res.ok) {
        alert('계정이 삭제되었습니다.');
        window.location.href = '/login';
    } else {
        var data = res ? await res.json().catch(function() { return null; }) : null;
        errEl.textContent = (data && data.message) || '계정 삭제 실패';
        errEl.style.display = 'block';
    }
}

/* ========== 탭 ========== */
function setupProfileTabs() {
    var tabs = document.getElementById('profileTabs');
    if (!tabs) return;

    // 본인 프로필에서만 저장됨 탭 표시
    var waitForProfile = setInterval(function() {
        if (profileData) {
            clearInterval(waitForProfile);
            if (!profileData.isOwnProfile) {
                var savedTab = tabs.querySelector('[data-tab="saved"]');
                if (savedTab) savedTab.style.display = 'none';
            }
        }
    }, 100);
    setTimeout(function() { clearInterval(waitForProfile); }, 3000);

    tabs.addEventListener('click', function(e) {
        var tab = e.target.closest('.tab');
        if (!tab) return;
        tabs.querySelectorAll('.tab').forEach(function(t) { t.classList.remove('active'); });
        tab.classList.add('active');
        currentTab = tab.dataset.tab;

        if (currentTab === 'saved') {
            loadBookmarks();
        } else {
            currentPage = 0;
            hasMore = true;
            loadUserPosts(0, false);
        }
    });
}

/* ========== 무한 스크롤 ========== */
function setupInfiniteScroll() {
    var sentinel = document.createElement('div');
    sentinel.id = 'scrollSentinel';
    sentinel.style.height = '1px';
    var postList = document.getElementById('postList');
    if (postList) postList.parentNode.insertBefore(sentinel, postList.nextSibling);

    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting && !isLoading && hasMore && currentTab === 'posts') {
            loadUserPosts(currentPage + 1, true);
        }
    }, { rootMargin: '200px' });

    observer.observe(sentinel);
}

/* ========== 초기화 ========== */
document.addEventListener('DOMContentLoaded', function() {
    loadProfile();
    setupProfileTabs();
    setupInfiniteScroll();

    // 오버레이 클릭 시 모달 닫기
    var overlay = document.getElementById('editOverlay');
    if (overlay) {
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) closeEditModal();
        });
    }
});

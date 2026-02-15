var currentPage = 0;
var currentTab = 'all';
var isLoading = false;
var hasMore = true;
var selectedImage = null;

/* ========== 피드 로딩 (무한 스크롤) ========== */
async function loadFeed(page, append) {
    if (isLoading) return;
    isLoading = true;

    var loader = document.getElementById('loadingIndicator');
    var emptyState = document.getElementById('emptyState');
    if (loader) loader.classList.remove('hidden');

    var res = await api('/api/posts?page=' + page + '&size=20&tab=' + currentTab);
    if (!res || !res.ok) { isLoading = false; if (loader) loader.classList.add('hidden'); return; }
    var data = await res.json();

    var feedList = document.getElementById('feedList');
    if (!append) feedList.innerHTML = '';

    if (data.posts.length === 0 && page === 0) {
        emptyState.classList.remove('hidden');
    } else {
        emptyState.classList.add('hidden');
    }

    data.posts.forEach(function(post) {
        feedList.appendChild(renderPostCard(post));
    });

    currentPage = data.page;
    hasMore = data.hasNext;

    if (loader) loader.classList.add('hidden');
    isLoading = false;
}

/* ========== 무한 스크롤 ========== */
function setupInfiniteScroll() {
    var sentinel = document.createElement('div');
    sentinel.id = 'scrollSentinel';
    sentinel.style.height = '1px';
    var feedList = document.getElementById('feedList');
    if (feedList) feedList.parentNode.insertBefore(sentinel, feedList.nextSibling);

    var observer = new IntersectionObserver(function(entries) {
        if (entries[0].isIntersecting && !isLoading && hasMore) {
            loadFeed(currentPage + 1, true);
        }
    }, { rootMargin: '200px' });

    observer.observe(sentinel);
}

/* ========== 탭 전환 ========== */
function setupTabs() {
    var tabs = document.getElementById('feedTabs');
    if (!tabs) return;
    tabs.addEventListener('click', function(e) {
        var tab = e.target.closest('.tab');
        if (!tab) return;
        tabs.querySelectorAll('.tab').forEach(function(t) { t.classList.remove('active'); });
        tab.classList.add('active');
        currentTab = tab.dataset.tab;
        currentPage = 0;
        hasMore = true;
        loadFeed(0, false);
    });
}

/* ========== 이미지 미리보기 ========== */
function previewImage(input) {
    if (input.files && input.files[0]) {
        selectedImage = input.files[0];
        var reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('imagePreviewImg').src = e.target.result;
            document.getElementById('imagePreview').style.display = 'block';
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function removeImage() {
    selectedImage = null;
    document.getElementById('imagePreview').style.display = 'none';
    document.getElementById('imagePreviewImg').src = '';
    document.getElementById('postImage').value = '';
}

/* ========== 게시글 작성 ========== */
async function createPost() {
    var content = document.getElementById('postContent').value.trim();
    if (!content) return;
    var visibility = document.getElementById('postVisibility').value;

    var res;
    if (selectedImage) {
        var formData = new FormData();
        formData.append('content', content);
        formData.append('visibility', visibility);
        formData.append('image', selectedImage);
        res = await api('/api/posts', {
            method: 'POST',
            body: formData
        });
    } else {
        res = await api('/api/posts', {
            method: 'POST',
            body: JSON.stringify({ content: content, visibility: visibility })
        });
    }

    if (res && res.ok) {
        document.getElementById('postContent').value = '';
        removeImage();
        document.getElementById('postVisibility').value = 'PUBLIC';
        currentPage = 0;
        hasMore = true;
        loadFeed(0, false);
    }
}

/* ========== 초기화 ========== */
document.addEventListener('DOMContentLoaded', function() {
    // 현재 사용자 정보 로딩 후 아바타 설정
    var waitForUser = setInterval(function() {
        if (currentUser !== null) {
            clearInterval(waitForUser);
            var avatar = document.getElementById('createPostAvatar');
            if (avatar) avatar.src = avatarUrl(currentUser.profileImageUrl);
        }
    }, 100);

    // 2초 후에도 사용자 정보 미로딩 시 폴백
    setTimeout(function() { clearInterval(waitForUser); }, 2000);

    loadFeed(0, false);
    setupInfiniteScroll();
    setupTabs();
});

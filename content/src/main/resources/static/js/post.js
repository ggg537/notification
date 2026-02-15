var currentPostId = window.location.pathname.split('/').pop();

async function loadPostDetail() {
    var res = await api('/api/posts/' + currentPostId);
    if (!res || !res.ok) {
        document.getElementById('postCard').innerHTML =
            '<div class="empty-state">게시글을 찾을 수 없거나 접근 권한이 없습니다.</div>';
        return;
    }
    var post = await res.json();

    var postCard = document.getElementById('postCard');
    var card = renderPostCard(post);

    // 작성자인 경우 수정/삭제 버튼 추가
    if (post.isOwner) {
        var ownerBar = document.createElement('div');
        ownerBar.style.cssText = 'display:flex; gap:8px; margin-bottom:14px;';
        ownerBar.innerHTML =
            '<button class="btn btn-outline btn-sm" onclick="editPost()">수정</button>' +
            '<button class="btn btn-danger btn-sm" onclick="deletePost()">삭제</button>';
        // 게시글 내용 뒤에 삽입
        var content = card.querySelector('.post-content');
        if (content) content.after(ownerBar);
    }

    postCard.innerHTML = '';
    postCard.appendChild(card);

    // 상세 페이지에서 댓글 자동 열기
    var commentBtn = card.querySelector('.comment-toggle-btn');
    if (commentBtn) toggleComments(commentBtn);
}

function editPost() {
    var contentEl = document.querySelector('.post-card .post-content');
    if (!contentEl) return;
    var currentContent = contentEl.textContent;
    contentEl.innerHTML =
        '<textarea class="edit-area" id="editArea" style="width:100%; padding:10px 14px; border:1px solid var(--border); border-radius:14px; background:var(--input-bg); color:var(--text); font-size:14px; resize:vertical; min-height:80px; font-family:inherit;">' +
        escapeHtml(currentContent) + '</textarea>' +
        '<div style="margin-top:8px; display:flex; gap:8px;">' +
            '<button class="btn btn-primary btn-sm" onclick="savePost()">저장</button>' +
            '<button class="btn btn-outline btn-sm" onclick="loadPostDetail()">취소</button>' +
        '</div>';
}

async function savePost() {
    var content = document.getElementById('editArea').value.trim();
    if (!content) return;
    var res = await api('/api/posts/' + currentPostId, {
        method: 'PUT',
        body: JSON.stringify({ content: content })
    });
    if (res && res.ok) loadPostDetail();
}

async function deletePost() {
    if (!confirm('이 게시글을 삭제하시겠습니까?')) return;
    var res = await api('/api/posts/' + currentPostId, { method: 'DELETE' });
    if (res && res.ok) window.location.href = '/';
}

document.addEventListener('DOMContentLoaded', function() {
    loadPostDetail();
});

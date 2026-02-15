var pivot = null;
var hasMore = true;

function renderNotification(n) {
    var item = document.createElement('div');
    item.className = 'notification-item';

    var iconClass = '';
    var icon = '';
    var text = '';
    var name = n.userName || '알 수 없는 사용자';

    if (n.type === 'COMMENT') {
        iconClass = 'comment';
        icon = '💬';
        text = '<strong>' + escapeHtml(name) + '</strong>님이 댓글을 남겼습니다: "' + escapeHtml(n.comment || '') + '"';
    } else if (n.type === 'LIKE') {
        iconClass = 'like';
        icon = '♥';
        var count = n.userCount || 1;
        text = '<strong>' + escapeHtml(name) + '</strong>';
        if (count > 1) text += '님 외 ' + (count - 1) + '명이';
        else text += '님이';
        text += ' 게시글에 좋아요를 눌렀습니다';
    } else if (n.type === 'FOLLOW') {
        iconClass = 'follow';
        icon = '●';
        text = '<strong>' + escapeHtml(name) + '</strong>님이 팔로우했습니다';
    }

    item.innerHTML =
        '<div class="notif-icon ' + iconClass + '">' + icon + '</div>' +
        '<div class="notif-text">' + text + '</div>' +
        '<div class="notif-time">' + timeAgo(n.occurredAt) + '</div>';

    return item;
}

async function loadNotifications() {
    if (!currentUser) return;
    var url = '/v1/user-notifications/' + currentUser.id;
    if (pivot) url += '?pivot=' + pivot;

    var res = await fetch(url);
    if (!res.ok) return;
    var data = await res.json();

    var list = document.getElementById('notificationList');
    var emptyState = document.getElementById('emptyState');
    var loadMoreBtn = document.getElementById('loadMoreBtn');

    if (data.notifications.length === 0 && !pivot) {
        emptyState.classList.remove('hidden');
        return;
    }
    emptyState.classList.add('hidden');

    data.notifications.forEach(function(n) {
        list.appendChild(renderNotification(n));
    });

    if (data.hasNext && data.pivot) {
        pivot = data.pivot;
        loadMoreBtn.classList.remove('hidden');
    } else {
        hasMore = false;
        loadMoreBtn.classList.add('hidden');
    }

    // 읽음 처리
    fetch('/v1/user-notifications/' + currentUser.id + '/read', { method: 'PUT' });
}

async function checkNewNotifications() {
    if (!currentUser) return;
    var res = await fetch('/v1/user-notifications/' + currentUser.id);
    if (!res.ok) return;
    var data = await res.json();

    var list = document.getElementById('notificationList');
    var existingCount = list.querySelectorAll('.notification-item').length;

    if (data.notifications.length === 0) return;

    if (data.notifications.length !== existingCount) {
        list.innerHTML = '';
        data.notifications.forEach(function(n) {
            var item = renderNotification(n);
            item.classList.add('new-item');
            list.appendChild(item);
        });
        document.getElementById('emptyState').classList.add('hidden');

        if (data.hasNext && data.pivot) {
            pivot = data.pivot;
            document.getElementById('loadMoreBtn').classList.remove('hidden');
        }

        fetch('/v1/user-notifications/' + currentUser.id + '/read', { method: 'PUT' });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    var waitForUser = setInterval(function() {
        if (currentUser) {
            clearInterval(waitForUser);
            loadNotifications();
            setInterval(checkNewNotifications, 3000);
        }
    }, 100);
});

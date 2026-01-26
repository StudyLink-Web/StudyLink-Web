document.addEventListener('DOMContentLoaded', () => {
    document.querySelector('.community_tab').addEventListener('click', e => {
        const li = e.target.closest('li');
        if (!li || !li.dataset.url) return;

        location.href = li.dataset.url;
    });
});
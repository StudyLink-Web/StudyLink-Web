document.addEventListener('DOMContentLoaded', () => {
    const tab = document.querySelector('.community_tab');
    if (!tab) return;

    tab.addEventListener('click', e => {
        const li = e.target.closest('li[data-url]');
        if (!li) return;

        location.href = li.dataset.url;
    });
});

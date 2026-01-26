const triggerEl = document.getElementById('trigger');
const fileEl = document.getElementById('file');
const zoneEl = document.getElementById('fileZone');
const regBtnEl = document.getElementById('regBtn');

triggerEl?.addEventListener('click', () => fileEl?.click());

// 실행파일 막기, 10MB 이상 사이즈 제한
const regExp = /\.(exe|sh|bat|jar|dll|msi)$/i;
const maxSize = 1024 * 1024 * 10;

function fileValid(fileName, fileSize) {
    if (regExp.test(fileName)) return 0;
    if (fileSize > maxSize) return 0;
    return 1;
}

fileEl?.addEventListener('change', (e) => {
    const fileObject = e.target.files;
    if (!zoneEl) return;

    if (regBtnEl) regBtnEl.disabled = false;
    zoneEl.innerHTML = '';

    if (!fileObject || fileObject.length === 0) return;

    let ul = `<ul class="list-group">`;
    let isOk = 1;

    for (const file of fileObject) {
        const valid = fileValid(file.name, file.size);
        isOk *= valid;

        ul += `<li class="list-group-item">`;
        ul += `<div class="mb-3">`;
        ul += valid
            ? `<div class="fw-bold mb-1">업로드 가능</div>`
            : `<div class="fw-bold mb-1 text-danger">업로드 불가능</div>`;
        ul += `${file.name} `;
        ul += `<span class="badge text-bg-${valid ? 'success' : 'danger'}">${file.size}Bytes</span>`;
        ul += `</div></li>`;
    }

    ul += `</ul>`;
    zoneEl.innerHTML = ul;

    if (isOk === 0 && regBtnEl) regBtnEl.disabled = true;
});

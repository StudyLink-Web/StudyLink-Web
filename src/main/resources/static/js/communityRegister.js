// a.js (수정본)

const fileInput = document.getElementById('file');
const trigger = document.getElementById('trigger');
const fileZone = document.getElementById('fileZone');
const regBtn = document.getElementById('regBtn');

if (trigger && fileInput) {
    trigger.addEventListener('click', () => fileInput.click());
}

const regExp = /\.(exe|sh|bat|jar|dll|msi)$/i;
const maxSize = 1024 * 1024 * 10;

function fileValid(fileName, fileSize) {
    if (regExp.test(fileName)) return false;
    if (fileSize > maxSize) return false;
    return true;
}

if (fileInput) {
    fileInput.addEventListener('change', () => {
        fileZone.innerHTML = '';

        const files = Array.from(fileInput.files || []);
        if (!files.length) {
            if (regBtn) regBtn.disabled = false;
            return;
        }

        let isOk = true;

        files.forEach(file => {
            const valid = fileValid(file.name, file.size);
            if (!valid) isOk = false;

            const box = document.createElement('div');
            box.style.width = '140px';
            box.style.textAlign = 'center';
            box.style.marginRight = '10px';

            if (file.type.startsWith('image/')) {
                const img = document.createElement('img');
                img.src = URL.createObjectURL(file);
                img.style.width = '100%';
                img.style.height = '100px';
                img.style.objectFit = 'cover';
                img.onload = () => URL.revokeObjectURL(img.src);
                box.appendChild(img);
            }

            const status = document.createElement('div');
            status.className = `fw-bold mb-1 ${valid ? '' : 'text-danger'}`;
            status.textContent = valid ? '업로드 가능' : '업로드 불가';
            box.appendChild(status);

            const name = document.createElement('div');
            name.style.fontSize = '12px';
            name.textContent = file.name;
            box.appendChild(name);

            const size = document.createElement('span');
            size.className = `badge text-bg-${valid ? 'success' : 'danger'}`;
            size.textContent = `${file.size} Bytes`;
            box.appendChild(size);

            fileZone.appendChild(box);
        });

        if (regBtn) {
            regBtn.disabled = !isOk; // ❗ 핵심 수정
        }
    });
}

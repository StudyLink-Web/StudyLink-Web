document.getElementById('trigger').addEventListener('click',()=>{
    document.getElementById('file').click();
});

// 실행파일 막기, 10MB 이상 사이즈 제한
const regExp = new RegExp("\.(exe|sh|bat|jar|dll|msi)$");
const maxSize = 1024*1024*10;

function fileVaild(fileName, fileSize){
    // 여러 파일에 대한 검증을 하기 위해 *=
    if(regExp.test(fileName)){
        return 0;
    } else if(fileSize > maxSize){
        return 0;
    }else{
        return 1;
    }
}

document.getElementById('file').addEventListener('change', (e)=>{
    const fileObject = e.target.files;
    const div = document.getElementById('fileZone');

    // 등록버튼 비활성화 풀기
    document.getElementById('regBtn').disabled = false;
    //이전 등록못하는 파일을 제거
    div.innerHTML = '';


    let ul = `<ul class="list-group">`;
    let isOk = 1; // 모든 첨부파일에 대한 검증 통과 결과 체크 *= 누적 곱
    for(let file of fileObject){
        let valid =  fileVaild(file.name, file.size);
        isOk *= valid;
        ul+=`<li class="list-group-item">`;
        ul+=`<div class="mb-3">`;
        ul+=`${valid ? '<div class="fw-bold mb-1">업로드 가능</div>' : '<div class="fw-bold mb-1 text-danger">업로드 불가능</div>'}`;
        ul+=`${file.name}  `;
        ul+=`<span class="badge text-bg-${valid ? 'success' : 'danger'}">${file.size}Bytes</span>`;
        ul+=`</div></li>`;
    }
    ul+=`</ul>`;
    div.innerHTML = ul;

    if(isOk == 0){
        // 검증을 통과 못했다면 버튼 비활성화
        document.getElementById('regBtn').disabled = true;
    }
})

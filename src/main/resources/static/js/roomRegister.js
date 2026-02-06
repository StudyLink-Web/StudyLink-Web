const registerBtn = document.getElementById('registerBtn');
const modal = document.getElementById('myModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const registerForm = document.getElementById('registerForm');
const exitBtn = document.getElementById('exitBtn');
let roleStrings = userRoles.map(r => r.authority);

const endModal = document.getElementById('endModal');
const closeEndModalBtn = document.getElementById('closeEndModalBtn');
const endForm = document.getElementById('endForm');

// 등록 버튼, 나가기 버튼 텍스트 방 상태, 유저 권한에 따라 바꾸기. 실제 처리는 서버에서
document.addEventListener('DOMContentLoaded', () => {
    if (roomDTO.status === 'TEMP') {
        // 학생만 temp방 생성 가능
        registerBtn.textContent = '등록';
        exitBtn.textContent = '취소';
    } else if (roomDTO.status === 'PENDING') {
        if (roleStrings.includes("ROLE_MENTOR")) {
            registerBtn.textContent = '문제풀이 시작';
            exitBtn.textContent = '나가기';
        } else if (roleStrings.includes("ROLE_STUDENT")) {
            if (senderId === roomDTO.studentId){
                // 본인 등록 문제
                registerBtn.textContent = '등록 취소';
                exitBtn.textContent = '나가기';
            } else {
                registerBtn.textContent = '등록 취소';
                registerBtn.style.disabled = 'true';
                exitBtn.textContent = '나가기';
            }
        } else {
            registerBtn.textContent = '문제풀이 시작';
            registerBtn.style.disabled = 'true';
            exitBtn.textContent = '나가기';
        }
    } else if (roomDTO.status === 'IN_PROGRESS') {
        if (roleStrings.includes("ROLE_MENTOR")) {
            registerBtn.textContent = '문제 풀이 완료';
            exitBtn.textContent = '문제 풀이 포기';
        } else { // 학생
            registerBtn.textContent = '문제 풀이 완료';
            registerBtn.style.disabled = 'true';
            exitBtn.textContent = '나가기';
        }
    } else if (roomDTO.status === 'ANSWERED') {
        if (roleStrings.includes("ROLE_MENTOR")) {
            registerBtn.textContent = '종료';
            registerBtn.style.disabled = 'true';
            exitBtn.textContent = '나가기';
        } else {
            registerBtn.textContent = '종료';
            exitBtn.textContent = '나가기';
        }
    } else { // COMPLETED
        if (roleStrings.includes("ROLE_MENTOR")) {
            registerBtn.textContent = '종료';
            registerBtn.style.disabled = 'true';
            exitBtn.textContent = '나가기';
        } else {
            registerBtn.textContent = '종료';
            registerBtn.style.disabled = 'true';
            exitBtn.textContent = '나가기';
        }
    }
});



// 문제 등록시 사용하는 모달 관련 코드
if (roomDTO.status === 'TEMP') {
    // 과목 select 버튼
    const subjectDiv = document.getElementById('subjectDiv');

    const wrapper = document.createElement('div');
    wrapper.className = 'custom-select';

    // 첫 번째 기본 선택 (화면에 표시할 이름)
    const selected = document.createElement('div');
    selected.className = 'selected';
    selected.textContent = subjectList[0].name; // 화면에 보여주는 이름

    const options = document.createElement('div');
    options.className = 'options';

    // hidden input (form submit용)
    const hiddenInput = document.createElement('input');
    hiddenInput.type = 'hidden';
    hiddenInput.name = 'subjectId';        // 서버에서 받을 이름
    hiddenInput.value = subjectList[0].subjectId; // 서버로 보낼 id

    // 옵션 생성
    subjectList.forEach(subject => {
        const option = document.createElement('div');
        option.className = 'option';
        option.textContent = subject.name; // 화면에는 이름

        option.addEventListener('click', () => {
            selected.textContent = subject.name;   // 화면 표시
            hiddenInput.value = subject.subjectId;       // 서버 전송용 id
            options.style.display = 'none';
        });

        options.appendChild(option);
    });

    // 클릭하면 드롭다운 열기/닫기
    selected.addEventListener('click', (e) => {
        e.stopPropagation();
        options.style.display =
            options.style.display === 'block' ? 'none' : 'block';
    });

    // 바깥 클릭 시 닫기
    document.addEventListener('click', () => {
        options.style.display = 'none';
    });

    wrapper.appendChild(selected);
    wrapper.appendChild(options);
    wrapper.appendChild(hiddenInput);
    subjectDiv.appendChild(wrapper);


    // 1대1 여부
    const checkbox = document.getElementById('assignMentorCheckbox');
    const mentorSelect = document.getElementById('mentorSelect');

    // 초기화
    mentorSelect.innerHTML = '';

    // favoriteList로 멘토 옵션 생성
    if (favoriteList.length > 0) {
        favoriteList.forEach(fav => {
            const option = document.createElement('option');
            option.value = fav.mentorId;       // 서버로 보낼 값
            option.textContent = fav.mentorName; // 화면 표시
            mentorSelect.appendChild(option);
        });

        // 체크박스에 따라 활성/비활성
        checkbox.addEventListener('change', () => {
            mentorSelect.disabled = !checkbox.checked;
        });

    } else {
        // 리스트가 비었으면 체크박스 비활성
        checkbox.disabled = true;
        checkbox.checked = false;
        mentorSelect.disabled = true;
        mentorSelect.value = '';
    }


    // point
    const pointDiv = document.getElementById('pointDiv');

    const points = [500, 1000, 1500];

    points.forEach((pointValue, index) => {
        const label = document.createElement('label');
        const radio = document.createElement('input');

        radio.type = 'radio';
        radio.name = 'point';        // 같은 name → 하나만 선택
        radio.value = pointValue;

        // 보유 포인트보다 작으면 활성, 작으면 disabled
        if (pointValue > userPoint) {
            radio.disabled = true;
        } else if (index === 0) {
            // 첫 번째 선택 가능 포인트를 기본 선택
            radio.checked = true;
        }

        label.appendChild(radio);
        label.appendChild(document.createTextNode(` ${pointValue}P`));
        label.appendChild(document.createElement('br'));

        pointDiv.appendChild(label);
    });


    // 제출 취소 버튼
    const btnDiv = document.getElementById('btnDiv');

    // 제출 버튼
    const submitBtn = document.createElement('button');
    submitBtn.type = 'submit';
    submitBtn.textContent = '제출';
    registerForm.appendChild(document.createElement('br'));
    registerForm.appendChild(document.createElement('br'));
    btnDiv.appendChild(submitBtn);

    // 포인트 라디오 전부 disabled인지 체크
    const pointRadios = pointDiv.querySelectorAll('input[type="radio"]');
    const allDisabled = Array.from(pointRadios).every(radio => radio.disabled);

    // 모두 disabled면 제출 버튼도 막기
    if (allDisabled) {
        submitBtn.disabled = true;
        submitBtn.style.opacity = '0.5';
        submitBtn.style.cursor = 'not-allowed';
        submitBtn.title = '보유 포인트가 부족합니다.';
    }

    // 취소 버튼
    const cancelBtn = document.createElement('button');
    cancelBtn.type = 'button';
    cancelBtn.textContent = '취소';
    registerForm.appendChild(document.createElement('br'));
    registerForm.appendChild(document.createElement('br'));
    btnDiv.appendChild(cancelBtn);
    // 취소 버튼 클릭
    cancelBtn.addEventListener('click', () => {
        closeModalBtn.click();
    })


}

// 모달 닫기
closeModalBtn.addEventListener('click', () => {
    modal.style.display = 'none';
});

// 종료 모달 닫기
closeEndModalBtn.addEventListener('click', () => {
    endModal.style.display = 'none';
});

// 모달 바깥 클릭 시 닫기
window.addEventListener('click', (event) => {
    if (event.target === modal) {
        modal.style.display = 'none';
    }
    if (event.target === endModal) {
        endModal.style.display = 'none';
    }
});


registerBtn.addEventListener('click', () => {
    if (roomDTO.status === 'TEMP') {
        modal.style.display = 'block';
    } else if (roomDTO.status === 'ANSWERED' && roleStrings.includes("ROLE_STUDENT")){
        // 학생이 종료 버튼 누른 경우
        // 종료 모달 띄우기
        // 만족도, 찜
        endModal.style.display = 'block';
    } else {
        window.location.href = `/room/updateState?roomId=${roomDTO.roomId}`;
    }
});


exitBtn.addEventListener('click', ()=>{
    if (roomDTO.status === 'TEMP') {
        // 학생이 등록 중 방 나가는 경우
        // 나가기 + temp방 삭제
        window.location.href = `/room/exitRoom?roomId=${roomDTO.roomId}`;
    } else if (roomDTO.status === 'IN_PROGRESS'){
        if (roleStrings.includes("ROLE_MENTOR")) {
            // 문제풀이 중 멘토가 포기하는 경우
            window.location.href = `/room/exitRoom?roomId=${roomDTO.roomId}`;
        } else {
            window.history.back();
        }
    } else {
        // 나머지는 그냥 나가기
        window.history.back();
    }
})


// 종료 모달
// 만족도
const stars = document.querySelectorAll('.star-rating span');
const ratingInput = document.getElementById('ratingValue');

stars.forEach(star => {
    star.addEventListener('click', () => {
        ratingInput.value = star.dataset.value;
        highlightStars(ratingInput.value);
    });
});

function highlightStars(value) {
    stars.forEach(star => {
        if (star.dataset.value <= value) {
            star.classList.add('selected');
        } else {
            star.classList.remove('selected');
        }
    });
}


document.addEventListener('DOMContentLoaded', () => {
    const favoriteCheckbox = document.getElementById('addFavoriteMentorCheckbox');

    // roomDTO.mentorId가 이미 favoriteList에 있는지 확인
    let isFavorited = null;
    if (favoriteList) {
        isFavorited = favoriteList.some(fav => fav.mentorId === roomDTO.mentorId);
    }


    if (isFavorited) {
        favoriteCheckbox.disabled = true;  // 체크박스 비활성화
        document.getElementById('favoriteMessage').style.display = 'block';
    }
});

// 제출 취소 버튼
const endFormBtnDiv = document.getElementById('endModalBtnDiv');
endForm.appendChild(endFormBtnDiv);

// 제출 버튼
const endFormSubmitBtn = document.createElement('button');
endFormSubmitBtn.type = 'submit';
endFormSubmitBtn.textContent = '제출';
endForm.appendChild(document.createElement('br'));
endForm.appendChild(document.createElement('br'));
endFormBtnDiv.appendChild(endFormSubmitBtn);


// 취소 버튼
const endFormCancelBtn = document.createElement('button');
endFormCancelBtn.type = 'button';
endFormCancelBtn.textContent = '취소';
endForm.appendChild(document.createElement('br'));
endForm.appendChild(document.createElement('br'));
endFormBtnDiv.appendChild(endFormCancelBtn);
// 취소 버튼 클릭
endFormCancelBtn.addEventListener('click', () => {
    closeEndModalBtn.click();
})
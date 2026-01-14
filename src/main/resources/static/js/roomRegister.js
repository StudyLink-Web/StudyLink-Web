const registerBtn = document.getElementById('registerBtn');
const modal = document.getElementById('myModal');
const closeBtn = document.querySelector('.close');
const registerForm = document.getElementById('registerForm');
console.log(subjectList)


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

// 🔥 favoriteList로 멘토 옵션 생성
favoriteList.forEach(fav => {
    const option = document.createElement('option');
    option.value = fav.mentorId;       // 서버로 보낼 값
    option.textContent = fav.mentorName; // 화면 표시
    mentorSelect.appendChild(option);
});

// 체크박스에 따라 활성/비활성
checkbox.addEventListener('change', () => {
    mentorSelect.disabled = !checkbox.checked;

    // 체크 해제 시 값 초기화
    if (!checkbox.checked) {
        mentorSelect.value = '';
    }
});



// point
const pointDiv = document.getElementById('pointDiv');

const points = [500, 1000, 1500];

points.forEach((point, index) => {
    const label = document.createElement('label');
    const radio = document.createElement('input');

    radio.type = 'radio';
    radio.name = 'point';        // 같은 name → 하나만 선택
    radio.value = point;

    // 첫 번째 기본 선택
    if (index === 0) radio.checked = true;

    label.appendChild(radio);
    label.appendChild(document.createTextNode(` ${point}P`));
    label.appendChild(document.createElement('br'));

    pointDiv.appendChild(label);
});




// 제출 취소 버튼
const btnDiv = document.getElementById('btnDiv');
registerForm.appendChild(btnDiv);

// 제출 버튼
const submitBtn = document.createElement('button');
submitBtn.type = 'submit';
submitBtn.textContent = '제출';
registerForm.appendChild(document.createElement('br'));
registerForm.appendChild(document.createElement('br'));
btnDiv.appendChild(submitBtn);


// 취소 버튼
const cancelBtn = document.createElement('button');
cancelBtn.type = 'button';
cancelBtn.textContent = '취소';
registerForm.appendChild(document.createElement('br'));
registerForm.appendChild(document.createElement('br'));
btnDiv.appendChild(cancelBtn);
// 취소 버튼 클릭
cancelBtn.addEventListener('click', ()=>{
    closeBtn.click();
})




// 모달 열기
registerBtn.addEventListener('click', () => {
    modal.style.display = 'block';
});

// 모달 닫기
closeBtn.addEventListener('click', () => {
    modal.style.display = 'none';
});

// 모달 바깥 클릭 시 닫기
window.addEventListener('click', (event) => {
    if(event.target === modal) {
        modal.style.display = 'none';
    }
});

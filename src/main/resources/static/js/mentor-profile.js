console.log('📋 mentor-profile.js 로드됨');

// ⭐ 즉시 실행 (DOMContentLoaded 제거)
console.log('🚀 탭 시스템 즉시 초기화');

// ✅ 탭 버튼들을 모두 찾기
const tabButtons = document.querySelectorAll('.tab-btn');
console.log('탭 버튼 개수:', tabButtons.length);

// ✅ 각 탭 버튼에 직접 클릭 이벤트 등록
tabButtons.forEach(btn => {
    console.log('탭 버튼 등록:', btn.dataset.tab);

    btn.onclick = function(e) {
        console.log('🔵 탭 클릭:', this.dataset.tab);
        e.preventDefault();
        e.stopPropagation();

        const tabName = this.dataset.tab;
        const tabElement = document.getElementById(tabName);

        if (!tabElement) {
            console.error('탭 없음:', tabName);
            return false;
        }

        // 모든 탭 비활성화
        document.querySelectorAll('.tab-btn').forEach(b => {
            b.classList.remove('active');
        });
        document.querySelectorAll('.tab-content').forEach(c => {
            c.classList.remove('active');
        });

        // 이 탭만 활성화
        this.classList.add('active');
        tabElement.classList.add('active');

        console.log('✅ 탭 변경:', tabName);
        return false;
    };
});

console.log('✅ 탭 시스템 준비 완료');

// ✅ 프로필 사진 업로드 미리보기
document.getElementById('avatarUpload')?.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (event) => {
            document.getElementById('avatarPreview').src = event.target.result;
        };
        reader.readAsDataURL(file);
    }
});

// ✅ 실시간 사이드바 업데이트
document.getElementById('firstName')?.addEventListener('input', (e) => {
    document.getElementById('sidebarName').textContent = e.target.value;
});

document.getElementById('nickname')?.addEventListener('input', (e) => {
    document.getElementById('sidebarNickname').textContent = e.target.value;
});

// ✅ 수정된 폼 제출 (FormData + 파일 업로드)
document.getElementById('profileForm')?.addEventListener('submit', (e) => {
    e.preventDefault();

    const newPass = document.getElementById('newPassword').value;
    const confirmPass = document.getElementById('confirmPassword').value;

    if (newPass || confirmPass) {
        if (newPass !== confirmPass) {
            showError('비밀번호가 일치하지 않습니다');
            return;
        }
        if (newPass.length < 8) {
            showError('비밀번호는 최소 8자 이상이어야 합니다');
            return;
        }
    }

    // ✅ FormData 객체 생성 (파일 업로드 가능)
    const formData = new FormData();

    // 기본 정보
    formData.append('firstName', document.getElementById('firstName').value);
    formData.append('nickname', document.getElementById('nickname').value);
    formData.append('phone', document.getElementById('phone').value);
    formData.append('bio', document.getElementById('bio').value);

    // 학력 정보
    formData.append('university', document.getElementById('university').value);
    formData.append('major', document.getElementById('major').value);
    formData.append('entranceYear', document.getElementById('entranceYear').value);
    formData.append('graduationYear', document.getElementById('graduationYear').value);
    formData.append('credentials', document.getElementById('credentials').value);

    // ✅ 과목 선택 (수정된 셀렉터)
    const subjects = Array.from(document.querySelectorAll('input[id^="subject"]:checked')).map(cb => cb.value);
    formData.append('subjects', JSON.stringify(subjects));

    // ✅ 학년 선택 (수정된 셀렉터)
    const grades = Array.from(document.querySelectorAll('input[id^="grade"]:checked')).map(cb => cb.value);
    formData.append('grades', JSON.stringify(grades));

    // 수업 정보
    formData.append('pricePerHour', document.getElementById('pricePerHour').value);
    formData.append('minLessonHours', document.getElementById('minLessonHours').value);
    formData.append('lessonType', document.querySelector('input[name="lessonType"]:checked')?.value || '');
    formData.append('lessonLocation', document.getElementById('lessonLocation').value);
    formData.append('availableTime', document.getElementById('availableTime').value);

    // 계정 설정
    formData.append('currentPassword', document.getElementById('currentPassword').value);
    formData.append('newPassword', document.getElementById('newPassword').value);
    formData.append('confirmPassword', document.getElementById('confirmPassword').value);
    formData.append('notificationLesson', document.getElementById('notifLesson').checked);
    formData.append('notificationMessage', document.getElementById('notifMessage').checked);
    formData.append('notificationReview', document.getElementById('notifReview').checked);

    // ✅ 📸 프로필 이미지 파일 추가
    const avatarFile = document.getElementById('avatarUpload').files[0];
    if (avatarFile) {
        formData.append('profileImage', avatarFile);
        console.log('📸 이미지 업로드:', avatarFile.name, avatarFile.size + 'bytes');
    }

    // ✅ FormData 전송 (Content-Type 헤더 자동 설정됨!)
    fetch('/mentor/update', {
        method: 'POST',
        body: formData  // ✅ FormData 사용 (헤더 제거!)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => Promise.reject(err));
            }
            return response.json();
        })
        .then(data => {
            if (data.message) {
                showSuccess(data.message);
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else if (data.error) {
                showError(data.error);
            }
        })
        .catch(error => {
            console.error('❌ Error:', error);
            showError(error.error || '프로필 저장 중 오류가 발생했습니다');
        });
});

function showSuccess(message) {
    const alert = document.getElementById('successAlert');
    alert.textContent = '✓ ' + message;
    alert.classList.add('show');
    setTimeout(() => alert.classList.remove('show'), 3000);
}

function showError(message) {
    const alert = document.getElementById('errorAlert');
    alert.textContent = '✗ ' + message;
    alert.classList.add('show');
    setTimeout(() => alert.classList.remove('show'), 3000);
}

function handleDeleteAccount() {
    if (confirm('정말 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        if (confirm('한 번 더 확인합니다. 계정을 삭제하시겠습니까?')) {
            fetch('/mentor/delete-account', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message) {
                        showSuccess('계정이 삭제되었습니다');
                        setTimeout(() => {
                            window.location.href = '/';
                        }, 2000);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showError('계정 삭제 중 오류가 발생했습니다');
                });
        }
    }
}

const subjectCheckboxes = document.querySelectorAll('#subject1, #subject2, #subject3, #subject4, #subject5');
const gradeCheckboxes = document.querySelectorAll('#grade1, #grade2, #grade3');

subjectCheckboxes.forEach(checkbox => {
    checkbox.addEventListener('change', () => {
        console.log('과목 선택 변경:', checkbox.value, checkbox.checked);
    });
});

gradeCheckboxes.forEach(checkbox => {
    checkbox.addEventListener('change', () => {
        console.log('학년 선택 변경:', checkbox.value, checkbox.checked);
    });
});


/* =========================
   ✅ 프로필 저장 전 필수 입력 검증
   - 전화번호 인증은 현재 필수 아님
========================= */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('profileForm');
    if (!form) return;

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const requiredFields = [
            { id: 'firstName', name: '이름' },
            { id: 'nickname', name: '닉네임' },
            { id: 'university', name: '대학교' },
            { id: 'major', name: '전공' },
            { id: 'entranceYear', name: '입학년도' },
            { id: 'graduationYear', name: '졸업년도' },
            { id: 'pricePerHour', name: '시간당 수업료' }
        ];

        for (const field of requiredFields) {
            const el = document.getElementById(field.id);
            if (!el || !el.value.trim()) {
                alert(`❗ ${field.name}을(를) 입력해주세요.`);
                el?.focus();
                return;
            }
        }

        const allSubjects = document.querySelectorAll(
            '.checkbox-group input[type="checkbox"][name="subjects"]'
        );
        const checkedSubjects = Array.from(allSubjects).filter((cb) => cb.checked);

        if (checkedSubjects.length === 0) {
            alert('❗ 최소 1개 이상의 과목을 선택해주세요.');
            document.getElementById('teaching').scrollIntoView({ behavior: 'smooth' });
            return;
        }

        console.log('✅ 선택된 과목:', checkedSubjects.map(cb => cb.value));

        /*
        const gradesChecked = document.querySelectorAll(
            '#teaching input[type="checkbox"][id^="grade"]:checked'
        );
        if (gradesChecked.length === 0) {
            alert('❗ 수업 대상 학년을 최소 1개 선택해주세요.');
            return;
        }
        */

        const lessonType = document.querySelector('input[name="lessonType"]:checked');
        if (!lessonType) {
            alert('❗ 수업 방식을 선택해주세요.');
            return;
        }

        /*
        // 🔒 전화번호 인증을 다시 필수로 만들 경우 사용
        if (!phoneAuthVerified) {
            alert('❗ 전화번호 인증을 완료해주세요.');
            return;
        }
        */

        // ✅ 전화번호 인증 여부는 검사하지 않음
        try {
            const formData = new FormData(form);

            console.log('📤 프로필 저장 중...');
            console.log('📋 선택된 과목:', checkedSubjects.map(cb => cb.value));

            const res = await fetch(form.action, {
                method: 'POST',
                body: formData
            });

            const contentType = res.headers.get('content-type');

            if (!contentType || !contentType.includes('application/json')) {
                const text = await res.text();
                console.error('❌ JSON 아님, 서버 응답:', text);
                throw new Error('서버가 JSON이 아닌 응답을 반환했습니다.');
            }

            const data = await res.json();

            alert(data.message || '✅ 프로필이 저장되었습니다.');

            // ✅ 저장된 값이 반영된 멘토 페이지 다시 로드
            location.reload();

        } catch (err) {
            console.error('❌ 프로필 저장 실패:', err);
            alert('프로필 저장 중 오류가 발생했습니다.');
        }
    });
});

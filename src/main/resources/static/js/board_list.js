document.addEventListener('DOMContentLoaded', () => {
  const registerBtn = document.querySelector('.register_campus_link');
  if (!registerBtn) return;

  registerBtn.style.display = 'none';

  const email = String(window.USER_EMAIL ?? '').trim().toLowerCase();
  const allow = window.IS_LOGIN === true && email === 'jeong36023610@gmail.com';

  if (allow) registerBtn.style.display = 'block';
});

function goRegister() {
  if (!window.IS_LOGIN) {
    alert('로그인이 필요합니다.');
    location.href = '/login';
    return;
  }

  const email = String(window.USER_EMAIL ?? '').trim().toLowerCase();
  if (email !== 'jeong36023610@gmail.com') {
    alert('권한이 없습니다.');
    return;
  }

  location.href = '/board/register';
}

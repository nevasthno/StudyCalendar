document.addEventListener('DOMContentLoaded', () => {
  loadProfile();

  document.getElementById("goBackToMainButton").addEventListener("click", () => {
    if (document.referrer) {
      window.location.href = document.referrer;
    } else {
      window.location.href = "login.html"; 
    }
  });

  const editForm = document.getElementById('editProfileForm');
  if (editForm) {
    editForm.addEventListener('submit', updateProfile);
  }
});

function loadProfile() {
  const token = getUserToken();
  if (!token) {
    console.error('Токен користувача відсутній');
    return;
  }

  fetch(`${getApiUrl()}/profile`, {
    headers: { 'Authorization': `Bearer ${token}` }
  })
  .then(res => {
    if (!res.ok) throw new Error('Помилка отримання профілю');
    return res.json();
  })
  .then(profile => {
    document.getElementById('profile-first-name').textContent = profile.firstName;
    document.getElementById('profile-last-name').textContent = profile.lastName ;
    document.getElementById('profile-date-of-birth').textContent = profile.dateOfBirth || '–';
    document.getElementById('profile-about-me').textContent = profile.aboutMe || '–';
    document.getElementById('profile-email').textContent = profile.email;
    document.getElementById('profile-role').textContent = profile.role;

    if (document.getElementById('new-firstName')) {
      document.getElementById('new-firstName').value = profile.firstName || '';
    }
    if (document.getElementById('new-lastname')) {
      document.getElementById('new-lastname').value = profile.lastName || '';
    }
    if (document.getElementById('new-aboutMe')) {
      document.getElementById('new-aboutMe').value = profile.aboutMe || '';
    }
    if (document.getElementById('new-dateOfBirth')) {
      document.getElementById('new-dateOfBirth').value = profile.dateOfBirth || '';
    }
    if (document.getElementById('new-email')) {
      document.getElementById('new-email').value = profile.email || '';
    }
  })
  .catch(err => {
    console.error('Помилка завантаження профілю:', err);
    alert('Не вдалося завантажити інформацію профілю.');
  });
}

function updateProfile(event) {
  event.preventDefault();
  const token = getUserToken();
  if (!token) {
    alert('Ви не авторизовані.');
    return;
  }

  const updatedProfile = {
    firstName: document.getElementById('new-firstName').value.trim(),
    lastName: document.getElementById('new-lastname').value.trim(),
    aboutMe: document.getElementById('new-aboutMe').value.trim(),
    dateOfBirth: document.getElementById('new-dateOfBirth').value,
    email: document.getElementById('new-email').value.trim(),
    password: document.getElementById('new-password').value 
  };

  fetch(`${getApiUrl()}/profile`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(updatedProfile)
  })
  .then(res => {
    if (!res.ok) throw new Error('Не вдалося оновити профіль');
    return res.json();
  })
  .then(data => {
    alert('Профіль оновлено');
    loadProfile(); 
    if (document.getElementById('new-password')) {
      document.getElementById('new-password').value = '';
    }
  })
  .catch(err => {
    console.error('Помилка оновлення профілю:', err);
    alert('Сталася помилка при оновленні профілю');
  });
}

function getUserToken() {
  return localStorage.getItem('token');
}

function getApiUrl() {
  return 'http://localhost:8080/api';
}

async function fetchWithAuth(url, opts = {}) {
  const token = localStorage.getItem("jwtToken");
  opts.headers = {
    ...(opts.headers || {}),
    "Authorization": `Bearer ${token}`,
    "Content-Type": "application/json"
  };
  return fetch(url, opts);
}
  
  document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("logoutButton")
      .addEventListener("click", () => {
        localStorage.removeItem("jwtToken");
        window.location.href = "login.html";
      });
    loadStats();
    loadUsers();
  
    document.getElementById("create-user-button")
      .addEventListener("click", async () => {
        const first = document.getElementById("new-user-first").value.trim();
        const last  = document.getElementById("new-user-last").value.trim();
        const email = document.getElementById("new-user-email").value.trim();
        const pass  = document.getElementById("new-user-pass").value;
        const role  = document.getElementById("new-user-role").value;
        if (!first || !last || !email || !pass || !role) {
          alert("Заповніть всі поля.");
          return;
        }
        try {
          const res = await fetchWithAuth("/api/users", {
            method: "POST",
            body: JSON.stringify({
              firstName: first,
              lastName:  last,
              email:     email,
              password:  pass,
              role:      role
            })
          });
          if (!res.ok) throw new Error(res.status);
          alert("Користувача створено!");
        } catch (e) {
          console.error("Помилка створення користувача:", e);
          alert("Не вдалося створити користувача.");
        }
      });
  
    document.getElementById("create-task-button")
      .addEventListener("click", async () => {
        const title   = document.getElementById("task-title").value.trim();
        const content = document.getElementById("task-content").value.trim();
        const deadlineStr = document.getElementById("task-deadline").value;

        if (!title || !deadlineStr) {
          alert("Вкажіть назву та дедлайн.");
          return;
        }

        const deadlineUtc = new Date(deadlineStr).toISOString(); 

        try {
          const res = await fetchWithAuth("/api/tasks", {
            method: "POST",
            body: JSON.stringify({
              title,
              content,
              deadline: deadlineUtc
            })
          });
          if (!res.ok) throw new Error(res.status);
          alert("Завдання додано!");
        } catch (e) {
          console.error("Помилка додавання завдання:", e);
          alert("Не вдалося додати завдання.");
        }
      });


      document.getElementById("create-event-button")
      .addEventListener("click", async () => {
        const userId = localStorage.getItem("userId");
        const title    = document.getElementById("event-title").value.trim();
        const content  = document.getElementById("event-content").value.trim();
        const loc      = document.getElementById("event-location").value.trim();
        const startStr = document.getElementById("event-start").value;
        const duration = parseInt(document.getElementById("event-duration").value, 10);
        const type     = document.getElementById("event-type").value;
      
        if (!title || !startStr || !duration || !type) {
          alert("Вкажіть обов’язкові поля.");
          return;
        }
      
        const localDate = new Date(startStr);
        const utcString = localDate.toISOString(); 
      
        try {
          await fetchWithAuth("/api/events", {
            method: "POST",
            body: JSON.stringify({
              title,
              content,
              location_or_link: loc,
              start_event: utcString,
              duration,
              event_type: type,
              created_by: userId
            })
          });
          alert("Подію створено!");
        } catch (e) {
          console.error("Помилка створення події:", e);
          alert("Не вдалося створити подію.");
        }
      });
  
    });
    async function loadStats() {
        try {
            const res = await fetchWithAuth("/api/stats");
            if (!res.ok) throw new Error(res.status);
            const stats = await res.json();
            document.getElementById("stat-total-tasks").textContent      = stats.totalTasks;
            document.getElementById("stat-completed-tasks").textContent  = stats.completedTasks;
            document.getElementById("stat-total-events").textContent     = stats.totalEvents;
        } catch (e) {
            console.error("Помилка завантаження статистики:", e);
        }
    }
    async function loadUsers() {
      try {
        const res = await fetchWithAuth("/api/loadUsers");
        if (!res.ok) throw new Error(res.status);
        const users = await res.json();

        const userList = document.getElementById("user-list");
        userList.innerHTML = "";

        users.forEach(user => {
          const li = document.createElement("li");
          li.textContent = `${user.firstName} ${user.lastName} (${user.email}) `;

          const editBtn = document.createElement("button");
          editBtn.textContent = "Змінити профіль";
          editBtn.style.marginLeft = "10px";

          editBtn.addEventListener("click", () => openEditForm(user));

          li.appendChild(editBtn);
          userList.appendChild(li);
        });

      } catch (e) {
        console.error("Помилка завантаження користувачів:", e);
      }
    }
    function openEditForm(user) {
      document.getElementById("edit-user-section").style.display = "block";

      document.getElementById("edit-user-id").value = user.id;
      document.getElementById("edit-firstName").value = user.firstName;
      document.getElementById("edit-lastName").value = user.lastName;
      document.getElementById("edit-aboutMe").value = user.aboutMe;
      document.getElementById("edit-dateOfBirth").value = user.dateOfBirth;
    }

    document.getElementById("edit-user-form").addEventListener("submit", async (e) => {
      e.preventDefault();

      const id = document.getElementById("edit-user-id").value;
      const firstName = document.getElementById("edit-firstName").value.trim();
      const lastName = document.getElementById("edit-lastName").value.trim();
      const aboutMe = document.getElementById("edit-aboutMe").value.trim();
      const dateOfBirth = document.getElementById("edit-dateOfBirth").value;

      if (!firstName || !lastName || !aboutMe || !dateOfBirth) {
        alert("Заповніть всі поля.");
        return;
      }

      const updateUserByTeacher = {
        firstName,
        lastName,
        aboutMe,
        dateOfBirth
      };

      try {
        const res = await fetchWithAuth(`/api/users/${id}`, {
          method: "PUT",
          body: JSON.stringify(updateUserByTeacher)
        });

        if (!res.ok) throw new Error(res.status);

        alert("Профіль оновлено!");
        document.getElementById("edit-user-section").style.display = "none";
        loadUsers();

      } catch (e) {
        console.error("Помилка оновлення користувача:", e);
        alert("Не вдалося оновити профіль.");
      }
    });

    document.getElementById("my-profile-button").addEventListener("click", () => {
  window.location.href = "profile.html";
});






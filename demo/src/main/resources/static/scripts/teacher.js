// Повторюємо fetchWithAuth з main.js
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
  
    // Створення користувача
    document.getElementById("create-user-button")
      .addEventListener("click", async () => {
        const first = document.getElementById("new-user-first").value.trim();
        const last  = document.getElementById("new-user-last").value.trim();
        const email = document.getElementById("new-user-email").value.trim();
        const role  = document.getElementById("new-user-role").value;
        const pass  = document.getElementById("new-user-pass").value;
        if (!first||!last||!email||!role||!pass) {
          alert("Заповніть всі поля.");
          return;
        }
        try {
          const res = await fetchWithAuth("/api/users", {
            method: "POST",
            body: JSON.stringify({ firstName: first, lastName: last, email, password: pass, role })
          });
          if (!res.ok) throw new Error(res.status);
          alert("Користувача створено!");
        } catch (e) {
          console.error("Помилка створення користувача:", e);
          alert("Не вдалося створити користувача.");
        }
      });
  
    // Створення завдання
    document.getElementById("create-task-button")
      .addEventListener("click", async () => {
        const title    = document.getElementById("task-title").value.trim();
        const content  = document.getElementById("task-content").value.trim();
        const deadline = document.getElementById("task-deadline").value;
        if (!title||!deadline) {
          alert("Вкажіть назву та дедлайн.");
          return;
        }
        try {
          await fetchWithAuth("/api/tasks", {
            method: "POST",
            body: JSON.stringify({ title, content, deadline })
          });
          alert("Завдання додано!");
        } catch (e) {
          console.error("Помилка додавання завдання:", e);
          alert("Не вдалося додати завдання.");
        }
      });
  
    document.getElementById("create-event-button")
      .addEventListener("click", async () => {
        const title    = document.getElementById("event-title").value.trim();
        const content  = document.getElementById("event-content").value.trim();
        const loc      = document.getElementById("event-location").value.trim();
        const start    = document.getElementById("event-start").value;
        const duration = parseInt(document.getElementById("event-duration").value, 10);
        const type     = document.getElementById("event-type").value;
        if (!title||!start||!duration||!type) {
          alert("Вкажіть обов’язкові поля.");
          return;
        }
        try {
          await fetchWithAuth("/api/events", {
            method: "POST",
            body: JSON.stringify({ title, content, location_or_link: loc, start_event: start, duration, event_type: type })
          });
          alert("Подію створено!");
        } catch (e) {
          console.error("Помилка створення події:", e);
          alert("Не вдалося створити подію.");
        }
      });
  });
  
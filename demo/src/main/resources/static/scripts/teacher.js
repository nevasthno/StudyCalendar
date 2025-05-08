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
    
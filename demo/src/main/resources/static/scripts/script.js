// script.js

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("loginButton")?.addEventListener("click", login);
    document.getElementById("logoutButton")?.addEventListener("click", logout);
  
    loadTasks();
    initCalendar();
    loadTeachers();
  });
  
  // Авторизація
  function login() {
    const user  = document.getElementById("login").value.trim();
    const pass  = document.getElementById("password").value.trim();
    const role  = document.getElementById("role").value;
  
    if (!user || !pass || !role) {
      alert("Будь ласка, заповніть всі поля.");
      return;
    }
  
    document.getElementById("login-section").classList.remove("active");
    document.getElementById("main-content").classList.add("active");
  }
  
  function logout() {
    document.getElementById("main-content").classList.remove("active");
    document.getElementById("login-section").classList.add("active");
  }
  
  // Завантаження вчителів
  async function loadTeachers() {
    const list = document.getElementById("teachers-list");
    if (!list) return;
    list.innerHTML = "";
    try {
      const res = await fetch("/api/teachers");
      const data = await res.json();
      data.forEach(t => {
        const li = document.createElement("li");
        li.textContent = `${t.firstName} ${t.lastName} (${t.email})`;
        list.appendChild(li);
      });
    } catch (err) {
      console.error("Помилка завантаження вчителів:", err);
    }
  }
  
  // Завантаження завдань
  async function loadTasks() {
    const list = document.getElementById("tasks-list");
    if (!list) return;
    list.innerHTML = "";
    try {
      const res = await fetch("/api/tasks");
      const data = await res.json();
      data.forEach(t => {
        const li = document.createElement("li");
        li.textContent = t.title;
        if (t.completed) li.classList.add("completed");
        list.appendChild(li);
      });
    } catch (err) {
      console.error("Помилка завантаження завдань:", err);
    }
  }
  
  // Календар
  let currentMonth, currentYear;
  
  function initCalendar() {
    const today = new Date();
    currentMonth = today.getMonth();
    currentYear  = today.getFullYear();
  
    document.getElementById("prev-month")
            .addEventListener("click", () => { changeMonth(-1); });
    document.getElementById("next-month")
            .addEventListener("click", () => { changeMonth(1); });
  
    updateCalendar();
  }
  
  function changeMonth(delta) {
    currentMonth += delta;
    if (currentMonth < 0) { currentMonth = 11; currentYear--; }
    if (currentMonth > 11){ currentMonth = 0;  currentYear++; }
    updateCalendar();
  }
  async function updateCalendar() {
    // 1) Забираем свежие события
    let events = [];
    try {
      const res = await fetch("/api/events");
      if (!res.ok) throw new Error(res.status);
      events = await res.json();
      console.log("Loaded events:", events); 
    } catch (err) {
      console.error("Не удалось загрузить события:", err);
    }
  
    // 2) Заголовок месяца
    const monthNameEl  = document.getElementById("month-name");
    const calendarBody = document.getElementById("calendar-body");
    if (!monthNameEl || !calendarBody) return;
  
    monthNameEl.textContent = new Intl.DateTimeFormat("uk", {
      month: "long", year: "numeric"
    }).format(new Date(currentYear, currentMonth));
  
    calendarBody.innerHTML = "";
  
    // 3) Смещаем первый день (понедельник=0..воскресенье=6)
    let firstDay = new Date(currentYear, currentMonth, 1).getDay();
    firstDay = firstDay === 0 ? 6 : firstDay - 1;
  
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    let date = 1;
  
    // 4) Генерируем ровно 6 строк
    for (let row = 0; row < 6; row++) {
      const tr = document.createElement("tr");
      for (let col = 0; col < 7; col++) {
        const td = document.createElement("td");
  
        if (row === 0 && col < firstDay) {
          // пустая клетка до начала месяца
        } else if (date > daysInMonth) {
          // пустая клетка после конца месяца
        } else {
          td.textContent = date;
          const D = String(date).padStart(2, "0");
          const M = String(currentMonth + 1).padStart(2, "0");
          const key = `${currentYear}-${M}-${D}`;
  
          // 4.1) Находим все события этого дня
          events
            .filter(e => (e.start_event || "").split("T")[0] === key)
            .forEach(ev => {
              const span = document.createElement("span");
              span.classList.add("event");
              span.textContent = ev.title;
              td.appendChild(span);
            });
  
          date++;
        }
  
        tr.appendChild(td);
      }
      calendarBody.appendChild(tr);
    }
  }
  
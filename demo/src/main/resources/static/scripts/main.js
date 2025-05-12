document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("logoutButton");
    if (btn) btn.addEventListener("click", logout);
  
    loadTasks();
    initCalendar();
    loadTeachers();
  });
  
  function logout() {
    localStorage.removeItem("jwtToken");
    window.location.href = "login.html";
  }
  
  async function fetchWithAuth(url, opts = {}) {
    const token = localStorage.getItem("jwtToken");
    opts.headers = {
      ...(opts.headers || {}),
      "Authorization": `Bearer ${token}`
    };
    return fetch(url, opts);
  }
  
  // TASKS
  async function loadTasks() {
    const list = document.getElementById("tasks-list");
    list.innerHTML = "";
    try {
      const res = await fetchWithAuth("/api/tasks");
      const tasks = await res.json();
      const today = new Date();
      const isToday = d => {
        const dt = new Date(d);
        return dt.getFullYear() === today.getFullYear() &&
               dt.getMonth() === today.getMonth() &&
               dt.getDate() === today.getDate();
      };
      tasks
        .filter(t => isToday(t.deadline))
        .sort((a,b) => new Date(a.deadline) - new Date(b.deadline))
        .forEach(t => {
          const li = document.createElement("li");
          li.textContent = `${t.title} (до ${new Date(t.deadline).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})})`;
          if (t.completed) li.classList.add("completed");
          li.addEventListener("click", async () => {
            await fetchWithAuth(`/api/tasks/${t.id}/toggle-complete`, { method: "POST" });
            loadTasks();
          });
          list.appendChild(li);
        });
    } catch (e) {
      console.error("Error loading tasks:", e);
    }
  }
  
  
  
  // TEACHERS
  async function loadTeachers() {
    const list = document.getElementById("teachers-list");
    if (!list) return;
    list.innerHTML = "";
    try {
      const res = await fetchWithAuth("/api/teachers");
      if (!res.ok) throw new Error(res.status);
      const teachers = await res.json();
      teachers.forEach(t => {
        const li = document.createElement("li");
        li.textContent = `${t.firstName} ${t.lastName} (${t.email})`;
        list.appendChild(li);
      });
    } catch (e) {
      console.error("Помилка завантаження викладачів:", e);
    }
  }
  
  // CALENDAR
  let currentMonth, currentYear;
  function initCalendar() {
    const now = new Date();
    currentMonth = now.getMonth();
    currentYear  = now.getFullYear();
  
    const prev = document.getElementById("prev-month");
    const next = document.getElementById("next-month");
    if (prev) prev.addEventListener("click", () => changeMonth(-1));
    if (next) next.addEventListener("click", () => changeMonth(1));
  
    updateCalendar();
  }
  
  function changeMonth(delta) {
    currentMonth += delta;
    if (currentMonth < 0) { currentMonth = 11; currentYear--; }
    if (currentMonth > 11){ currentMonth = 0; currentYear++; }
    updateCalendar();
  }
  
  async function updateCalendar() {
    let events = [];
    try {
      const res = await fetchWithAuth("/api/events");
      if (!res.ok) throw new Error(res.status);
      events = await res.json();
    } catch (e) {
      console.error("Помилка завантаження подій:", e);
    }
  
    const mm = document.getElementById("month-name");
    const body = document.getElementById("calendar-body");
    if (!mm || !body) return;
    body.innerHTML = "";
  
    mm.textContent = new Intl.DateTimeFormat("uk", {
      month: "long", year: "numeric"
    }).format(new Date(currentYear, currentMonth));
  
    let fd = new Date(currentYear, currentMonth, 1).getDay();
    fd = (fd === 0 ? 6 : fd - 1);  // понедельник=0 … воскресенье=6
  
    const daysInMonth = new Date(currentYear, currentMonth+1, 0).getDate();
    let d = 1;
  
    for (let r = 0; r < 6; r++) {
      const tr = document.createElement("tr");
      for (let c = 0; c < 7; c++) {
        const td = document.createElement("td");
        if (!(r === 0 && c < fd) && d <= daysInMonth) {
          td.textContent = d;
          const key = `${currentYear}-${String(currentMonth+1).padStart(2,"0")}-${String(d).padStart(2,"0")}`;
          events
            .filter(e => (e.start_event||"").split("T")[0] === key)
            .forEach(ev => {
              const sp = document.createElement("span");
              sp.classList.add("event");
              sp.textContent = ev.title;
              td.appendChild(sp);
            });
          d++;
        }
        tr.appendChild(td);
      }
      body.appendChild(tr);
      if (d > daysInMonth) break;
    }
  }
  
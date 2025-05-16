async function fetchWithAuth(url, opts = {}) {
  const token = localStorage.getItem("jwtToken");
  opts.headers = {
    ...(opts.headers||{}),
    "Authorization": `Bearer ${token}`
  };
  return fetch(url, opts);
}

let schoolId, classId;

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("logoutButton")
          .addEventListener("click", logout);
  initUserContext();
});

function logout() {
  localStorage.removeItem("jwtToken");
  window.location.href = "login.html";
}

async function initUserContext() {
  const res = await fetchWithAuth("/api/profile");
  if (!res.ok) {
    console.error("Не вдалося отримати профіль:", res.status);
    return;
  }
  const me = await res.json();
  schoolId = me.schoolId;
  classId  = me.classId;
  loadTasks();
  initCalendar();
  loadTeachers();
}

async function loadTasks() {
  const list = document.getElementById("tasks-list");
  list.innerHTML = "";
  const params = new URLSearchParams();
  if (schoolId) params.set("schoolId", schoolId);
  if (classId)  params.set("classId", classId);
  const res = await fetchWithAuth("/api/tasks?" + params);
  if (!res.ok) {
    console.error("Не вдалося завантажити завдання:", res.status);
    return;
  }
  const tasks = await res.json();
  const today = new Date();
  tasks
    .filter(t => new Date(t.deadline).toDateString() === today.toDateString())
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
}

async function loadTeachers() {
  const list = document.getElementById("teachers-list");
  list.innerHTML = "";
  const params = new URLSearchParams();
  if (schoolId) params.set("schoolId", schoolId);
  if (classId)  params.set("classId", classId);
  const res = await fetchWithAuth("/api/teachers?" + params);
  if (!res.ok) {
    console.error("Не вдалося завантажити вчителів:", res.status);
    return;
  }
  const ppl = await res.json();
  ppl.forEach(t => {
    const li = document.createElement("li");
    li.textContent = `${t.firstName} ${t.lastName} (${t.email})`;
    list.appendChild(li);
  });
}

let currentMonth=0, currentYear=0;
function initCalendar() {
  const now = new Date();
  currentMonth = now.getMonth();
  currentYear  = now.getFullYear();
  document.getElementById("prev-month")?.addEventListener("click", () => changeMonth(-1));
  document.getElementById("next-month")?.addEventListener("click", () => changeMonth(1));
  updateCalendar();
}

async function updateCalendar() {
  const qs = new URLSearchParams();
  if (schoolId) qs.set("schoolId", schoolId);
  if (classId)  qs.set("classId", classId);

  const res = await fetchWithAuth("/api/events?" + qs);
  if (!res.ok) {
    console.error("Не вдалося завантажити події:", res.status);
    return;
  }
  const events = await res.json();

  const mm = document.getElementById("month-name");
  const body = document.getElementById("calendar-body");
  if (!mm || !body) return;

  body.innerHTML = "";
  mm.textContent = new Intl.DateTimeFormat("uk", { month:"long", year:"numeric" })
                    .format(new Date(currentYear, currentMonth));

  let fd = new Date(currentYear, currentMonth, 1).getDay();
  fd = fd === 0 ? 6 : fd - 1;
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
          .filter(e => {
            if (typeof e.start_event !== "string") return false;
            return e.start_event.split("T")[0] === key;
          })
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



function changeMonth(delta) {
  currentMonth = (currentMonth + delta + 12) % 12;
  if (delta===-1 && currentMonth===11) currentYear--;
  if (delta===1  && currentMonth===0 ) currentYear++;
  updateCalendar();
}

async function fetchWithAuth(url, opts = {}) {
  const token = localStorage.getItem("jwtToken");
  opts.headers = {
    ...(opts.headers || {}),
    "Authorization": `Bearer ${token}`
  };
  return fetch(url, opts);
}

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("logoutButton")?.addEventListener("click", () => {
    localStorage.removeItem("jwtToken");
    window.location.href = "login.html";
  });

  loadInvitations();
  initCalendar();
  loadProfile();

  document.getElementById("goBackToMainButton")?.addEventListener("click", () => {
    if (document.referrer) {
      window.location.href = document.referrer;
    } else {
      window.location.href = "login.html"; 
    }
  });

  document.getElementById('editProfileForm')?.addEventListener('submit', updateProfile);
});

async function loadInvitations() {
  const list = document.getElementById("invitations-list");
  if (!list) return;

  list.innerHTML = "";
  try {
    const res = await fetchWithAuth("/api/events");
    const events = await res.json();

    events
      .filter(e => e.event_type === "PARENTS_MEETING")
      .sort((a, b) => new Date(a.start_event) - new Date(b.start_event))
      .forEach(e => {
        const li = document.createElement("li");
        const dt = new Date(e.start_event);
        li.textContent = `${e.title} — ${dt.toLocaleDateString("uk")}, ${dt.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}`;
        list.appendChild(li);
      });
  } catch (e) {
    console.error("Помилка завантаження запрошень", e);
  }
}

let currentMonth, currentYear;

function initCalendar() {
  const now = new Date();
  currentMonth = now.getMonth();
  currentYear  = now.getFullYear();

  document.getElementById("prev-month")?.addEventListener("click", () => changeMonth(-1));
  document.getElementById("next-month")?.addEventListener("click", () => changeMonth(1));

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
    events = await res.json();
  } catch (e) {
    console.error("Помилка завантаження подій календаря", e);
  }

  const monthNameEl  = document.getElementById("month-name");
  const calendarBody = document.getElementById("calendar-body");
  if (!monthNameEl || !calendarBody) return;

  calendarBody.innerHTML = "";
  monthNameEl.textContent = new Intl.DateTimeFormat("uk", {
    month: "long", year: "numeric"
  }).format(new Date(currentYear, currentMonth));

  let firstDay = new Date(currentYear, currentMonth, 1).getDay();
  firstDay = firstDay === 0 ? 6 : firstDay - 1;
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  let date = 1;

  for (let row = 0; row < 6; row++) {
    const tr = document.createElement("tr");
    for (let col = 0; col < 7; col++) {
      const td = document.createElement("td");
      if (!(row === 0 && col < firstDay) && date <= daysInMonth) {
        td.textContent = date;
        const key = `${currentYear}-${String(currentMonth+1).padStart(2,"0")}-${String(date).padStart(2,"0")}`;
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
    if (date > daysInMonth) break;
  }
}

async function loadProfile() {
  try {
    const res = await fetchWithAuth("/api/me");
    if (!res.ok) throw new Error(res.status);
    const user = await res.json();

    document.getElementById("profile-firstName").textContent = user.firstName || "-";
    document.getElementById("profile-lastName").textContent = user.lastName || "-";
    document.getElementById("profile-aboutMe").textContent = user.aboutMe || "-";
    document.getElementById("profile-dateOfBirth").textContent = user.dateOfBirth || "-";
    document.getElementById("profile-email").textContent = user.email || "-";
    document.getElementById("profile-role").textContent = user.role || "-";

    document.getElementById("edit-firstName").value = user.firstName || "";
    document.getElementById("edit-lastName").value = user.lastName || "";
    document.getElementById("edit-aboutMe").value = user.aboutMe || "";
    document.getElementById("edit-dateOfBirth").value = user.dateOfBirth || "";
    document.getElementById("edit-email").value = user.email || "";
  } catch (e) {
    console.error("Помилка завантаження профілю", e);
    alert("Не вдалося завантажити профіль. Спробуйте ще раз.");
  }
}

async function updateProfile(event) {
  event.preventDefault();
  const form = event.target;
  const formData = new FormData(form);
  const data = Object.fromEntries(formData);

  if (data.password !== data.confirmPassword) {
    alert("Паролі не збігаються!");
    return;
  }

  delete data.confirmPassword;
  if (!data.password) delete data.password;

  Object.keys(data).forEach(key => {
    if (data[key] === "") delete data[key];
  });

  try {
    const res = await fetchWithAuth("/api/me", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    if (!res.ok) throw new Error(res.status);
    alert("Профіль успішно оновлено!");
    loadProfile(); 
  } catch (e) {
    console.error("Помилка оновлення профілю", e);
    alert("Не вдалося оновити профіль.");
  }
}

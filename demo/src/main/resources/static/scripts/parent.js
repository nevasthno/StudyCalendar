async function fetchWithAuth(url, opts = {}) {
  const token = localStorage.getItem("jwtToken");
  opts.headers = {
    ...(opts.headers || {}),
    "Authorization": `Bearer ${token}`
  };
  return fetch(url, opts);
}

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("logoutButton")
          .addEventListener("click", () => {
    localStorage.removeItem("jwtToken");
    window.location.href = "login.html";
  });

  loadInvitations();
  initCalendar();
});

async function loadInvitations() {
  const list = document.getElementById("invitations-list");
  list.innerHTML = "";
  try {
    const res = await fetchWithAuth("/api/events");
    const events = await res.json();
    // фільтруємо лише події типу PARENTS_MEETING
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
    console.error(e);
  }
}

let currentMonth, currentYear;
function initCalendar() {
  const now = new Date();
  currentMonth = now.getMonth();
  currentYear  = now.getFullYear();
  document.getElementById("prev-month")
          .addEventListener("click", () => changeMonth(-1));
  document.getElementById("next-month")
          .addEventListener("click", () => changeMonth(1));
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
    console.error(e);
  }

  const monthNameEl  = document.getElementById("month-name");
  const calendarBody = document.getElementById("calendar-body");
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
          .filter(e => (e.start_event||"").split("T")[0] === key)
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

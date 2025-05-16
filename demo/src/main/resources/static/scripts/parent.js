async function fetchWithAuth(url, opts = {}) {
    const token = localStorage.getItem("jwtToken");
    opts.headers = {
        ...(opts.headers || {}),
        "Authorization": `Bearer ${token}`
    };
    return fetch(url, opts);
}

let schoolId = null, classId = null;
let currentMonth, currentYear;

document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("logoutButton");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("jwtToken");
            window.location.href = "login.html";
        });
    }
    initSelectors();
});

async function initSelectors() {
    const schoolSel = document.getElementById("school-select");
    const classSel = document.getElementById("class-select");
    // If selectors are missing, do not block calendar/invitations rendering
    // Remove the early return so the rest of the page works without selectors
    // if (!schoolSel || !classSel) return;

    if (schoolSel && classSel) {
        const resS = await fetchWithAuth("/api/schools");
        const schools = await resS.json();
        schoolSel.innerHTML = `<option value=''>Оберіть школу</option>`;
        schools.forEach(s => {
            schoolSel.innerHTML += `<option value="${s.id}">${s.name}</option>`;
        });

        schoolSel.onchange = async () => {
            schoolId = schoolSel.value || null;
            classSel.innerHTML = `<option>Завантаження...</option>`;
            if (!schoolId) {
                classSel.innerHTML = `<option value=''>Усі класи</option>`;
                classId = null;
                await loadInvitations();
                await updateCalendar();
                return;
            }
            const resC = await fetchWithAuth(`/api/classes?schoolId=${schoolId}`);
            const classes = await resC.json();
            classSel.innerHTML = `<option value=''>Усі класи</option>`;
            classes.forEach(c => {
                classSel.innerHTML += `<option value="${c.id}">${c.name}</option>`;
            });
            classId = null;
            await loadInvitations();
            await updateCalendar();
        };

        classSel.onchange = async () => {
            classId = classSel.value || null;
            await loadInvitations();
            await updateCalendar();
        };
    }

    await loadInvitations();
    await initCalendar();
}

async function loadInvitations() {
    const list = document.getElementById("invitations-list");
    if (!list) return;
    list.innerHTML = "";
    let events = [];
    try {
        const res = await fetchWithAuth("/api/events");
        events = await res.json();
    } catch (e) {
        list.innerHTML = "<li>Не вдалося завантажити події</li>";
        return;
    }
    // Accept both event_type and eventType for compatibility
    const filtered = events.filter(e =>
        e.event_type === "PARENTS_MEETING" ||
        (e.event_type && e.event_type.name === "PARENTS_MEETING") ||
        e.eventType === "PARENTS_MEETING"
    );
    if (filtered.length === 0) {
        list.innerHTML = "<li>Немає запрошень</li>";
        return;
    }
    filtered
        .sort((a, b) => new Date(a.start_event) - new Date(b.start_event))
        .forEach(e => {
            const dt = new Date(e.start_event);
            const li = document.createElement("li");
            li.textContent = `${e.title} — ${dt.toLocaleDateString("uk")}, ${dt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
            list.appendChild(li);
        });
}

async function initCalendar() {
    const now = new Date();
    if (typeof currentMonth !== "number" || typeof currentYear !== "number") {
        currentMonth = now.getMonth();
        currentYear = now.getFullYear();
    }

    const prevBtn = document.getElementById("prev-month");
    const nextBtn = document.getElementById("next-month");
    if (prevBtn) prevBtn.onclick = () => changeMonth(-1);
    if (nextBtn) nextBtn.onclick = () => changeMonth(1);

    // Ensure calendar table and section are visible
    const calendarTable = document.getElementById("calendar-table");
    const calendarSection = document.getElementById("calendar-section");
    if (calendarTable) calendarTable.style.display = "table";
    if (calendarSection) calendarSection.style.display = "";

    await updateCalendar();
}

function changeMonth(delta) {
    currentMonth += delta;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    } else if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    updateCalendar();
}

async function updateCalendar() {
    const qs = new URLSearchParams();
    if (schoolId) qs.set("schoolId", schoolId);
    if (classId) qs.set("classId", classId);
    let events = [];
    try {
        const res = await fetchWithAuth("/api/events?" + qs);
        events = await res.json();
    } catch (e) {
        events = [];
    }

    const mm = document.getElementById("month-name");
    const body = document.getElementById("calendar-body");
    if (mm) mm.style.display = "";
    if (body) body.style.display = "";
    if (!mm || !body) return;

    // Parse event dates for fast lookup
    const eventsByDay = {};
    events.forEach(ev => {
        if (!ev.start_event) return;
        // Accept both "YYYY-MM-DD" and "YYYY-MM-DDTHH:MM:SS" formats
        const dateStr = ev.start_event.slice(0, 10);
        eventsByDay[dateStr] = eventsByDay[dateStr] || [];
        eventsByDay[dateStr].push(ev);
    });

    body.innerHTML = "";
    mm.textContent = new Intl.DateTimeFormat("uk", { month: "long", year: "numeric" })
        .format(new Date(currentYear, currentMonth));

    let fd = new Date(currentYear, currentMonth, 1).getDay();
    fd = fd === 0 ? 6 : fd - 1;
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    let d = 1;
    for (let r = 0; r < 6; r++) {
        const tr = document.createElement("tr");
        for (let c = 0; c < 7; c++) {
            const td = document.createElement("td");
            if ((r === 0 && c < fd) || d > daysInMonth) {
                td.innerHTML = "&nbsp;";
            } else {
                td.textContent = d;
                const key = `${currentYear}-${String(currentMonth + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
                // Render all events for this day
                (eventsByDay[key] || []).forEach(ev => {
                    const sp = document.createElement("span");
                    sp.classList.add("event");
                    sp.textContent = ev.title;
                    td.appendChild(document.createElement("br"));
                    td.appendChild(sp);
                });
                d++;
            }
            tr.appendChild(td);
        }
        body.appendChild(tr);
    }
}

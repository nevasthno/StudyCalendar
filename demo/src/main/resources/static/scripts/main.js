// Tab/page switching logic
document.addEventListener("DOMContentLoaded", function () {
    const tabMain = document.getElementById("tab-main");
    const tabProfile = document.getElementById("tab-profile");
    const mainPage = document.getElementById("main-page");
    const profilePage = document.getElementById("profile-page");

    function showPage(page) {
        mainPage.classList.remove("active");
        profilePage.classList.remove("active");
        tabMain.classList.remove("active");
        tabProfile.classList.remove("active");
        if (page === "main") {
            mainPage.classList.add("active");
            tabMain.classList.add("active");
        } else {
            profilePage.classList.add("active");
            tabProfile.classList.add("active");
        }
    }

    if (tabMain && tabProfile && mainPage && profilePage) {
        tabMain.addEventListener("click", () => showPage("main"));
        tabProfile.addEventListener("click", () => {
            showPage("profile");
            loadProfile(); // <-- force reload profile when tab is shown
        });
    }
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

let currentMonth, currentYear, currentDay, currentView = "month";
let calendarUserId = null; // null = self

document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("logoutButton");
    if (btn) btn.addEventListener("click", logout);

    loadTasks();
    initCalendar();
    loadTeachers();
    loadEvents();
    loadProfile();

    const editForm = document.getElementById('editProfileForm');
    if (editForm) {
        editForm.addEventListener('submit', updateProfile);
    }

    const goBackBtn = document.getElementById("goBackToMainButton");
    if (goBackBtn) {
        goBackBtn.addEventListener("click", () => {
            window.location.href = document.referrer || "login.html";
        });
    }

    const titleInput = document.getElementById("searchTitle");
    const dateInput = document.getElementById("searchDate");
    const statusSelect = document.getElementById("filterStatus");
    const timeSelect = document.getElementById("filterTime");

    [titleInput, dateInput, statusSelect, timeSelect].forEach(el => {
        if (el) el.addEventListener("input", loadEvents);
    });

    // Calendar view buttons
    document.getElementById("calendar-view-day").addEventListener("click", () => switchCalendarView("day"));
    document.getElementById("calendar-view-week").addEventListener("click", () => switchCalendarView("week"));
    document.getElementById("calendar-view-month").addEventListener("click", () => switchCalendarView("month"));
    document.getElementById("calendar-view-year").addEventListener("click", () => switchCalendarView("year"));

    // Calendar navigation
    document.getElementById("prev-period").addEventListener("click", () => changePeriod(-1));
    document.getElementById("next-period").addEventListener("click", () => changePeriod(1));

    // User selector
    loadCalendarUserSelector();
    document.getElementById("calendar-user-select").addEventListener("change", function () {
        calendarUserId = this.value || null;
        updateCalendar();
    });

    initCalendar();
});

function switchCalendarView(view) {
    currentView = view;
    document.getElementById("calendar-view-day").classList.toggle("active", view === "day");
    document.getElementById("calendar-view-week").classList.toggle("active", view === "week");
    document.getElementById("calendar-view-month").classList.toggle("active", view === "month");
    document.getElementById("calendar-view-year").classList.toggle("active", view === "year");
    updateCalendar();
}

function changePeriod(delta) {
    if (currentView === "month") {
        currentMonth += delta;
        if (currentMonth < 0) { currentMonth = 11; currentYear--; }
        if (currentMonth > 11) { currentMonth = 0; currentYear++; }
    } else if (currentView === "week") {
        const date = new Date(currentYear, currentMonth, currentDay || 1);
        date.setDate(date.getDate() + delta * 7);
        currentYear = date.getFullYear();
        currentMonth = date.getMonth();
        currentDay = date.getDate();
    } else if (currentView === "day") {
        const date = new Date(currentYear, currentMonth, currentDay || 1);
        date.setDate(date.getDate() + delta);
        currentYear = date.getFullYear();
        currentMonth = date.getMonth();
        currentDay = date.getDate();
    } else if (currentView === "year") {
        currentYear += delta;
    }
    updateCalendar();
}

function initCalendar() {
    const now = new Date();
    currentMonth = now.getMonth();
    currentYear = now.getFullYear();
    currentDay = now.getDate();
    currentView = "month";
    updateCalendar();
}

async function loadCalendarUserSelector() {
    const sel = document.getElementById("calendar-user-select");
    if (!sel) return;
    try {
        const res = await fetchWithAuth("/api/loadUsers");
        if (!res.ok) throw new Error(res.status);
        const users = await res.json();
        // Only show students and parents
        sel.innerHTML = `<option value="">Я</option>`;
        users
            .filter(u => u.role === "STUDENT" || u.role === "PARENT")
            .forEach(u => {
                sel.innerHTML += `<option value="${u.id}">${u.firstName} ${u.lastName} (${u.email})</option>`;
            });
    } catch (e) {
        sel.innerHTML = `<option value="">Я</option>`;
    }
}

async function loadTasks() {
    const list = document.getElementById("tasks-list");
    if (!list) return;
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
            .sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
            .forEach(t => {
                const li = document.createElement("li");
                li.textContent = `${t.title} (до ${new Date(t.deadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })})`;
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

async function updateCalendar() {
    let events = [];
    let url = "/api/events";
    if (calendarUserId) {
        url = `/api/events?userId=${calendarUserId}`;
    }
    try {
        const res = await fetchWithAuth(url);
        if (!res.ok) throw new Error(res.status);
        events = await res.json();
    } catch (e) {
        events = [];
    }

    // Hide all views
    document.getElementById("calendar-table").style.display = "none";
    document.getElementById("calendar-day-view").style.display = "none";
    document.getElementById("calendar-week-view").style.display = "none";
    document.getElementById("calendar-year-view").style.display = "none";

    if (currentView === "month") {
        renderMonthView(events);
    } else if (currentView === "week") {
        renderWeekView(events);
    } else if (currentView === "day") {
        renderDayView(events);
    } else if (currentView === "year") {
        renderYearView(events);
    }
}

function renderMonthView(events) {
    const mm = document.getElementById("period-name");
    const body = document.getElementById("calendar-body");
    document.getElementById("calendar-table").style.display = "";
    if (!mm || !body) return;
    body.innerHTML = "";

    mm.textContent = new Intl.DateTimeFormat("uk", {
        month: "long", year: "numeric"
    }).format(new Date(currentYear, currentMonth));

    let fd = new Date(currentYear, currentMonth, 1).getDay();
    fd = (fd === 0 ? 6 : fd - 1);

    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    let d = 1;

    for (let r = 0; r < 6; r++) {
        const tr = document.createElement("tr");
        for (let c = 0; c < 7; c++) {
            const td = document.createElement("td");
            if (!(r === 0 && c < fd) && d <= daysInMonth) {
                td.textContent = d;
                const key = `${currentYear}-${String(currentMonth + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
                events
                    .filter(e => (e.start_event || "").split("T")[0] === key)
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

function renderDayView(events) {
    const container = document.getElementById("calendar-day-view");
    container.style.display = "";
    document.getElementById("period-name").textContent = new Date(currentYear, currentMonth, currentDay).toLocaleDateString("uk-UA", { weekday: "long", year: "numeric", month: "long", day: "numeric" });
    container.innerHTML = "";
    const key = `${currentYear}-${String(currentMonth + 1).padStart(2, "0")}-${String(currentDay).padStart(2, "0")}`;
    const dayEvents = events.filter(e => (e.start_event || "").split("T")[0] === key);
    if (dayEvents.length === 0) {
        container.innerHTML = "<div style='color:#bbb;text-align:center;'>Подій немає</div>";
    } else {
        dayEvents.forEach(ev => {
            const div = document.createElement("div");
            div.className = "event-card";
            div.innerHTML = `<div class="event-title">${ev.title}</div>
                <div class="event-date">${ev.start_event ? new Date(ev.start_event).toLocaleString("uk-UA") : ""}</div>
                ${ev.location_or_link ? `<div><b>Місце/посилання:</b> ${ev.location_or_link}</div>` : ""}
                ${ev.content ? `<div>${ev.content}</div>` : ""}
                ${ev.event_type ? `<div><b>Тип:</b> ${ev.event_type.name || ev.event_type}</div>` : ""}`;
            container.appendChild(div);
        });
    }
}

function renderWeekView(events) {
    const container = document.getElementById("calendar-week-view");
    container.style.display = "";
    container.innerHTML = "";

    // Find Monday of current week
    const date = new Date(currentYear, currentMonth, currentDay);
    const dayOfWeek = (date.getDay() + 6) % 7; // Monday=0
    const monday = new Date(date);
    monday.setDate(date.getDate() - dayOfWeek);

    document.getElementById("period-name").textContent =
        "Тиждень: " +
        monday.toLocaleDateString("uk-UA", { day: "numeric", month: "short" }) +
        " - " +
        new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + 6).toLocaleDateString("uk-UA", { day: "numeric", month: "short", year: "numeric" });

    // Flex row for week days
    const weekRow = document.createElement("div");
    weekRow.style.display = "flex";
    weekRow.style.gap = "14px";
    weekRow.style.justifyContent = "space-between";

    for (let i = 0; i < 7; i++) {
        const d = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + i);
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
        const dayCol = document.createElement("div");
        dayCol.style.flex = "1";
        dayCol.style.background = "#333";
        dayCol.style.color = "#fff";
        dayCol.style.borderRadius = "8px";
        dayCol.style.padding = "10px";
        dayCol.style.minWidth = "120px";
        dayCol.style.boxShadow = "0 1px 4px rgba(0,0,0,0.07)";
        dayCol.style.display = "flex";
        dayCol.style.flexDirection = "column";
        dayCol.style.alignItems = "stretch";
        dayCol.style.minHeight = "140px";

        // Day header
        const dayHeader = document.createElement("div");
        dayHeader.style.fontWeight = "bold";
        dayHeader.style.color = "#ff4c4c";
        dayHeader.style.marginBottom = "6px";
        dayHeader.textContent = d.toLocaleDateString("uk-UA", { weekday: "short", day: "numeric" });
        dayCol.appendChild(dayHeader);

        // Events
        const dayEvents = events.filter(e => (e.start_event || "").split("T")[0] === key);
        if (dayEvents.length === 0) {
            const noEv = document.createElement("div");
            noEv.style.color = "#bbb";
            noEv.style.textAlign = "center";
            noEv.textContent = "—";
            dayCol.appendChild(noEv);
        } else {
            dayEvents.forEach(ev => {
                const div = document.createElement("div");
                div.className = "event-card";
                div.style.marginBottom = "8px";
                div.innerHTML = `<div class="event-title">${ev.title}</div>
                    <div class="event-date">${ev.start_event ? new Date(ev.start_event).toLocaleTimeString("uk-UA", { hour: '2-digit', minute: '2-digit' }) : ""}</div>
                    ${ev.location_or_link ? `<div><b>Місце/посилання:</b> ${ev.location_or_link}</div>` : ""}
                    ${ev.content ? `<div>${ev.content}</div>` : ""}
                    ${ev.event_type ? `<div><b>Тип:</b> ${ev.event_type.name || ev.event_type}</div>` : ""}`;
                dayCol.appendChild(div);
            });
        }
        weekRow.appendChild(dayCol);
    }
    container.appendChild(weekRow);
}

function renderYearView(events) {
    const container = document.getElementById("calendar-year-view");
    container.style.display = "";
    container.innerHTML = "";
    document.getElementById("period-name").textContent = `Рік: ${currentYear}`;

    // 12 months, 3 per row
    const monthsPerRow = 3;
    for (let row = 0; row < 4; row++) {
        const rowDiv = document.createElement("div");
        rowDiv.style.display = "flex";
        rowDiv.style.gap = "16px";
        for (let m = row * monthsPerRow; m < (row + 1) * monthsPerRow; m++) {
            const monthDiv = document.createElement("div");
            monthDiv.style.flex = "1";
            monthDiv.style.background = "#eee";
            monthDiv.style.color = "#222";
            monthDiv.style.borderRadius = "8px";
            monthDiv.style.padding = "8px";
            monthDiv.style.minWidth = "180px";
            monthDiv.style.marginBottom = "16px";
            monthDiv.style.boxShadow = "0 1px 4px rgba(0,0,0,0.07)";

            monthDiv.innerHTML = `<div style="font-weight:bold;color:#ff4c4c;text-align:center;">${new Date(currentYear, m).toLocaleString("uk-UA", { month: "long" })}</div>`;

            // Month calendar table
            const table = document.createElement("table");
            table.style.width = "100%";
            table.style.background = "#fff";
            table.style.marginTop = "4px";
            const thead = document.createElement("thead");
            const trHead = document.createElement("tr");
            ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"].forEach(day => {
                const th = document.createElement("th");
                th.textContent = day;
                th.style.fontSize = "0.85em";
                trHead.appendChild(th);
            });
            thead.appendChild(trHead);
            table.appendChild(thead);

            const tbody = document.createElement("tbody");
            let fd = new Date(currentYear, m, 1).getDay();
            fd = (fd === 0 ? 6 : fd - 1);
            const daysInMonth = new Date(currentYear, m + 1, 0).getDate();
            let d = 1;
            for (let r = 0; r < 6; r++) {
                const tr = document.createElement("tr");
                for (let c = 0; c < 7; c++) {
                    const td = document.createElement("td");
                    if (!(r === 0 && c < fd) && d <= daysInMonth) {
                        td.textContent = d;
                        const key = `${currentYear}-${String(m + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
                        const dayEvents = events.filter(e => (e.start_event || "").split("T")[0] === key);
                        if (dayEvents.length > 0) {
                            td.style.background = "#dbeafe";
                            td.style.borderRadius = "4px";
                            dayEvents.forEach(ev => {
                                const sp = document.createElement("span");
                                sp.classList.add("event");
                                sp.textContent = ev.title;
                                td.appendChild(document.createElement("br"));
                                td.appendChild(sp);
                            });
                        }
                        d++;
                    } else {
                        td.innerHTML = "&nbsp;";
                    }
                    tr.appendChild(td);
                }
                tbody.appendChild(tr);
                if (d > daysInMonth) break;
            }
            table.appendChild(tbody);
            monthDiv.appendChild(table);
            rowDiv.appendChild(monthDiv);
        }
        container.appendChild(rowDiv);
    }
}

async function loadEvents() {
    const container = document.getElementById("eventsContainer");
    if (!container) return;
    container.innerHTML = "Завантаження...";

    const titleSearch = (document.getElementById("searchTitle")?.value || "").toLowerCase();
    const dateSearch = document.getElementById("searchDate")?.value || "";
    const statusFilter = document.getElementById("filterStatus")?.value || "ALL";
    const timeFilter = document.getElementById("filterTime")?.value || "ALL";

    try {
        const res = await fetchWithAuth("/api/events");
        if (!res.ok) throw new Error(res.status);

        const events = await res.json();
        const filteredEvents = events.filter(event => {
            const matchesTitle = event.title.toLowerCase().includes(titleSearch);
            const matchesDate = !dateSearch || event.start_event.startsWith(dateSearch);
            const matchesStatus = statusFilter === "ALL" || event.status === statusFilter;
            const matchesTime = timeFilter === "ALL" || event.time === timeFilter;

            return matchesTitle && matchesDate && matchesStatus && matchesTime;
        });

        container.innerHTML = "";
        if (filteredEvents.length === 0) {
            container.innerHTML = "<div style='color:#bbb;text-align:center;'>Подій не знайдено</div>";
        }
        filteredEvents.forEach(event => {
            const div = document.createElement("div");
            div.className = "event-card";
            div.innerHTML = `
                <div class="event-title">${event.title}</div>
                <div class="event-date">${event.start_event ? new Date(event.start_event).toLocaleString("uk-UA") : ""}</div>
                ${event.location_or_link ? `<div><b>Місце/посилання:</b> ${event.location_or_link}</div>` : ""}
                ${event.content ? `<div>${event.content}</div>` : ""}
                ${event.event_type ? `<div><b>Тип:</b> ${event.event_type.name || event.event_type}</div>` : ""}
            `;
            container.appendChild(div);
        });
    } catch (e) {
        console.error("Помилка завантаження подій:", e);
        container.innerHTML = "Не вдалося завантажити події.";
    }
}

async function loadProfile() {
    try {
        const res = await fetchWithAuth("/api/me");
        if (!res.ok) throw new Error(res.status);

        const user = await res.json();

        // Set text for spans (display)
        [
            ["profile-firstName", user.firstName],
            ["profile-lastName", user.lastName],
            ["profile-aboutMe", user.aboutMe],
            ["profile-dateOfBirth", user.dateOfBirth],
            ["profile-email", user.email],
            ["profile-role", user.role]
        ].forEach(([id, value]) => {
            const el = document.getElementById(id);
            if (el && el.tagName !== "INPUT" && el.tagName !== "TEXTAREA") {
                el.textContent = value && value !== "" ? value : "-";
            }
        });

        // Set value for inputs (edit form)
        [
            ["edit-firstName", user.firstName],
            ["edit-lastName", user.lastName],
            ["edit-aboutMe", user.aboutMe],
            ["edit-dateOfBirth", user.dateOfBirth],
            ["edit-email", user.email]
        ].forEach(([id, value]) => {
            const el = document.getElementById(id);
            if (el && (el.tagName === "INPUT" || el.tagName === "TEXTAREA")) {
                el.value = value && value !== "" ? value : "";
            }
        });
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

    // Remove confirmPassword from payload
    delete data.confirmPassword;

    // Remove password if empty
    if (!data.password) delete data.password;

    // Remove dateOfBirth if empty
    if (!data.dateOfBirth) delete data.dateOfBirth;

    // Remove aboutMe if empty
    if (!data.aboutMe) delete data.aboutMe;

    // Remove any other empty fields
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

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
});

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

let currentMonth, currentYear;

function initCalendar() {
    const now = new Date();
    currentMonth = now.getMonth();
    currentYear = now.getFullYear();

    const prev = document.getElementById("prev-month");
    const next = document.getElementById("next-month");
    if (prev) prev.addEventListener("click", () => changeMonth(-1));
    if (next) next.addEventListener("click", () => changeMonth(1));

    updateCalendar();
}

function changeMonth(delta) {
    currentMonth += delta;
    if (currentMonth < 0) { currentMonth = 11; currentYear--; }
    if (currentMonth > 11) { currentMonth = 0; currentYear++; }
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

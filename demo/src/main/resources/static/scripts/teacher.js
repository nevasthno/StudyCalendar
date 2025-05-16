async function fetchWithAuth(url, opts = {}) {
    const token = localStorage.getItem("jwtToken");
    opts.headers = {
        ...(opts.headers || {}),
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };
    return fetch(url, opts);
}

let schoolId = null, classId = null;

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("logoutButton").addEventListener("click", () => {
        localStorage.removeItem("jwtToken");
        window.location.href = "login.html";
    });

    initSelectors();
});

async function initSelectors() {
    const schoolSel = document.getElementById("school-select");
    const classSel = document.getElementById("class-select");

    // Початкові підказки
    schoolSel.innerHTML = "<option value=''>Завантаження шкіл...</option>";
    classSel.innerHTML = "<option value=''>Оберіть школу спочатку</option>";

    const resS = await fetchWithAuth("/api/schools");
    const schools = await resS.json();
    schoolSel.innerHTML = "<option value=''>Оберіть школу</option>";
    schools.forEach(s => {
        const opt = document.createElement("option");
        opt.value = s.id;
        opt.textContent = s.name;
        schoolSel.appendChild(opt);
    });

    schoolSel.addEventListener("change", async () => {
        schoolId = schoolSel.value;
        classSel.innerHTML = "<option value=''>Завантаження класів...</option>";
        if (!schoolId) {
            classSel.innerHTML = "<option value=''>Оберіть школу</option>";
            classId = null;
            loadStats();
            return;
        }
        const resC = await fetchWithAuth(`/api/classes?schoolId=${schoolId}`);
        const classes = await resC.json();
        classSel.innerHTML = "<option value=''>Усі класи</option>";
        classes.forEach(c => {
            const opt = document.createElement("option");
            opt.value = c.id;
            opt.textContent = c.name;
            classSel.appendChild(opt);
        });
        classId = "";
        loadStats();
    });

    classSel.addEventListener("change", () => {
        classId = classSel.value;
        loadStats();
    });

    document.getElementById("create-user-button").addEventListener("click", createUser);
    document.getElementById("create-task-button").addEventListener("click", createTask);
    document.getElementById("create-event-button").addEventListener("click", createEvent);
}

async function loadStats() {
    const res = await fetchWithAuth(`/api/stats?schoolId=${schoolId}&classId=${classId}`);
    const stats = await res.json();
    document.getElementById("stat-total-tasks").textContent = stats.totalTasks;
    document.getElementById("stat-completed-tasks").textContent = stats.completedTasks;
    document.getElementById("stat-total-events").textContent = stats.totalEvents;
}

async function createUser() {
    const first = document.getElementById("new-user-first").value.trim();
    const last = document.getElementById("new-user-last").value.trim();
    const email = document.getElementById("new-user-email").value.trim();
    const pass = document.getElementById("new-user-pass").value;
    const role = document.getElementById("new-user-role").value;
    if (!first || !last || !email || !pass || !role || !schoolId) {
        return alert("Заповніть всі поля та оберіть школу.");
    }
    const body = {
        firstName: first,
        lastName: last,
        email,
        password: pass,
        role,
        schoolId,
        classId: classId ? classId : null // якщо classId порожній, передаємо null
    };
    const res = await fetchWithAuth("/api/users", { method: "POST", body: JSON.stringify(body) });
    if (res.ok) alert("Користувача створено!");
    else alert("Не вдалося створити користувача.");
}

async function createTask() {
    const title = document.getElementById("task-title").value.trim();
    const content = document.getElementById("task-content").value.trim();
    const dl = document.getElementById("task-deadline").value;
    if (!title || !dl || !schoolId) {
        return alert("Вкажіть назву, дедлайн та школу.");
    }
    const body = {
        title,
        content,
        deadline: new Date(dl).toISOString(),
        schoolId,
        classId: classId ? classId : null // якщо classId порожній, передаємо null
    };
    const res = await fetchWithAuth("/api/tasks", { method: "POST", body: JSON.stringify(body) });
    if (res.ok) alert("Завдання додано!");
    else alert("Не вдалося додати завдання.");
}

async function createEvent() {
    const title = document.getElementById("event-title").value.trim();
    const content = document.getElementById("event-content").value.trim();
    const loc = document.getElementById("event-location").value.trim();
    const start = document.getElementById("event-start").value;
    const duration = +document.getElementById("event-duration").value;
    const type = document.getElementById("event-type").value;
    if (!title || !start || !duration || !type || !schoolId) {
        return alert("Вкажіть усі обов’язкові поля та школу.");
    }
    const body = {
        title,
        content,
        location_or_link: loc,
        start_event: new Date(start).toISOString(),
        duration,
        event_type: type,
        schoolId,
        classId: classId ? classId : null // якщо classId порожній, передаємо null
    };
    const res = await fetchWithAuth("/api/events", { method: "POST", body: JSON.stringify(body) });
    if (res.ok) alert("Подію створено!");
    else alert("Не вдалося створити подію.");
}

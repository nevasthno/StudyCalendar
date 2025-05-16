// Tab/page switching logic
document.addEventListener("DOMContentLoaded", function () {
    const tabUsers = document.getElementById("tab-users");
    const tabCreate = document.getElementById("tab-create");
    const usersPage = document.getElementById("users-page");
    const createPage = document.getElementById("create-page");

    function showPage(page) {
        usersPage.classList.remove("active");
        createPage.classList.remove("active");
        tabUsers.classList.remove("active");
        tabCreate.classList.remove("active");
        if (page === "users") {
            usersPage.classList.add("active");
            tabUsers.classList.add("active");
        } else {
            createPage.classList.add("active");
            tabCreate.classList.add("active");
        }
    }

    if (tabUsers && tabCreate && usersPage && createPage) {
        tabUsers.addEventListener("click", () => showPage("users"));
        tabCreate.addEventListener("click", () => showPage("create"));
    }
});

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

    const logoutBtn = document.getElementById("logoutButton");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("jwtToken");
            window.location.href = "login.html";
        });
    }


    loadStats();
    loadUsers();
    loadProfile();

    const goBackBtn = document.getElementById("goBackToMainButton");
    if (goBackBtn) {
        goBackBtn.addEventListener("click", () => {
            if (document.referrer) {
                window.location.href = document.referrer;
            } else {
                window.location.href = "login.html";
            }
        });
    }

    const editProfileForm = document.getElementById("editProfileForm");
    if (editProfileForm) {
        editProfileForm.addEventListener("submit", updateProfile);
    }


    const createUserBtn = document.getElementById("create-user-button");
    if (createUserBtn) {
        createUserBtn.addEventListener("click", async () => {
            const first = document.getElementById("new-user-first").value.trim();
            const last = document.getElementById("new-user-last").value.trim();
            const email = document.getElementById("new-user-email").value.trim();
            const pass = document.getElementById("new-user-pass").value;
            const role = document.getElementById("new-user-role").value;

            if (!first || !last || !email || !pass || !role) {
                alert("Заповніть всі поля.");
                return;
            }

            try {
                const res = await fetchWithAuth("/api/users", {
                    method: "POST",
                    body: JSON.stringify({ firstName: first, lastName: last, email, password: pass, role })
                });
                if (!res.ok) throw new Error(res.status);
                alert("Користувача створено!");
                loadUsers();
            } catch (e) {
                console.error("Помилка створення користувача:", e);
                alert("Не вдалося створити користувача.");
            }
        });
    }


    const createTaskBtn = document.getElementById("create-task-button");
    if (createTaskBtn) {
        createTaskBtn.addEventListener("click", async () => {
            const title = document.getElementById("task-title").value.trim();
            const content = document.getElementById("task-content").value.trim();
            const deadlineStr = document.getElementById("task-deadline").value;

            if (!title || !deadlineStr) {
                alert("Вкажіть назву та дедлайн.");
                return;
            }

            try {
                const res = await fetchWithAuth("/api/tasks", {
                    method: "POST",
                    body: JSON.stringify({ title, content, deadline: new Date(deadlineStr).toISOString() })
                });
                if (!res.ok) throw new Error(res.status);
                alert("Завдання додано!");
            } catch (e) {
                console.error("Помилка додавання завдання:", e);
                alert("Не вдалося додати завдання.");
            }
        });
    }


    const createEventBtn = document.getElementById("create-event-button");
    if (createEventBtn) {
        createEventBtn.addEventListener("click", async () => {
            const userId = localStorage.getItem("userId");
            const title = document.getElementById("event-title").value.trim();
            const content = document.getElementById("event-content").value.trim();
            const loc = document.getElementById("event-location").value.trim();
            const startStr = document.getElementById("event-start").value;
            const duration = parseInt(document.getElementById("event-duration").value, 10);
            const type = document.getElementById("event-type").value;

            if (!title || !startStr || !duration || !type) {
                alert("Вкажіть обов’язкові поля.");
                return;
            }

            try {
                await fetchWithAuth("/api/events", {
                    method: "POST",
                    body: JSON.stringify({
                        title,
                        content,
                        location_or_link: loc,
                        start_event: new Date(startStr).toISOString(),
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
    }


    const editUserForm = document.getElementById("edit-user-form");
    if (editUserForm) {
        editUserForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const id = document.getElementById("edit-user-id").value;
            const firstName = document.getElementById("edit-firstName").value.trim();
            const lastName = document.getElementById("edit-lastName").value.trim();
            const aboutMe = document.getElementById("edit-aboutMe").value.trim();
            const dateOfBirth = document.getElementById("edit-dateOfBirth").value;

            if (!firstName || !lastName || !aboutMe || !dateOfBirth) {
                alert("Заповніть всі поля.");
                return;
            }

            try {
                const res = await fetchWithAuth(`/api/users/${id}`, {
                    method: "PUT",
                    body: JSON.stringify({ firstName, lastName, aboutMe, dateOfBirth })
                });

                if (!res.ok) throw new Error(res.status);
                alert("Профіль оновлено!");
                document.getElementById("edit-user-section").style.display = "none";
                loadUsers();
            } catch (e) {
                console.error("Помилка оновлення користувача:", e);
                alert("Не вдалося оновити профіль.");
            }
        });
    }
});


async function loadStats() {
    try {
        const res = await fetchWithAuth("/api/stats");
        if (!res.ok) throw new Error(res.status);
        const stats = await res.json();
        document.getElementById("stat-total-tasks").textContent = stats.totalTasks;
        document.getElementById("stat-completed-tasks").textContent = stats.completedTasks;
        document.getElementById("stat-total-events").textContent = stats.totalEvents;
    } catch (e) {
        console.error("Помилка завантаження статистики:", e);
    }
}

async function loadUsers() {
    try {
        const res = await fetchWithAuth("/api/loadUsers");
        if (!res.ok) throw new Error(res.status);
        const users = await res.json();

        const userList = document.getElementById("user-list");
        if (!userList) return;

        userList.innerHTML = "";
        users.forEach(user => {
            const li = document.createElement("li");
            li.textContent = `${user.firstName} ${user.lastName} (${user.email})`;

            const editBtn = document.createElement("button");
            editBtn.textContent = "Змінити профіль";
            editBtn.style.marginLeft = "10px";
            editBtn.addEventListener("click", () => openEditForm(user));

            li.appendChild(editBtn);
            userList.appendChild(li);
        });
    } catch (e) {
        console.error("Помилка завантаження користувачів:", e);
    }
}

function openEditForm(user) {
    document.getElementById("edit-user-section").style.display = "block";
    document.getElementById("edit-user-id").value = user.id;
    document.getElementById("edit-firstName").value = user.firstName;
    document.getElementById("edit-lastName").value = user.lastName;
    document.getElementById("edit-aboutMe").value = user.aboutMe;
    document.getElementById("edit-dateOfBirth").value = user.dateOfBirth;
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

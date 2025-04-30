document.getElementById("loginButton").addEventListener("click", async () => {
    const user = document.getElementById("login").value.trim();
    const pass = document.getElementById("password").value.trim();
    const role = document.getElementById("role").value;
  
    if (!user || !pass || !role) {
      alert("Будь ласка, заповніть всі поля.");
      return;
    }
  
    try {
        const res = await fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              email: document.getElementById("login").value.trim(),
              password: document.getElementById("password").value.trim()
            })
          });
          if (!res.ok) {
            const err = await res.json();
            alert(err.error || `Status ${res.status}`);
            return;
          }
          const { token } = await res.json();
          localStorage.setItem("jwtToken", token);
          window.location.href = "main.html";
    } catch (err) {
      console.error("Помилка входу:", err);
      alert("Не вдалося увійти.");
    }
  });
  
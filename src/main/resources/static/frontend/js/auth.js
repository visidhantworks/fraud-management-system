function saveSession(loginResponse) {
  if (!loginResponse || !loginResponse.token) {
    throw new Error("Login response did not contain a JWT token.");
  }
  // Only token + basic identity/role are persisted. Password/PIN are never stored.
  localStorage.setItem("frm_token", loginResponse.token);
  localStorage.setItem("frm_user", JSON.stringify({
    userId: loginResponse.userId,
    name: loginResponse.name,
    email: loginResponse.email,
    role: loginResponse.role
  }));
}

function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem("frm_user") || "null");
  } catch (_) {
    return null;
  }
}

function logout() {
  clearAuthStorage();
  location.href = "login.html";
}

function requireAuth(requiredRole = null) {
  const token = getToken();
  const user = getCurrentUser();

  if (!token || !user) {
    location.href = "login.html";
    return null;
  }

  if (requiredRole && user.role !== requiredRole) {
    location.href = user.role === "ADMIN" ? "admin-dashboard.html" : "user-dashboard.html";
    return null;
  }
  return user;
}

function setupLogout() {
  const button = document.getElementById("logoutButton");
  if (button) button.addEventListener("click", logout);
}

function setupMobileMenu() {
  const button = document.getElementById("menuButton");
  const sidebar = document.getElementById("sidebar");
  if (button && sidebar) button.addEventListener("click", () => sidebar.classList.toggle("open"));
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");
  if (form) {
    // Login page should not retain an authenticated session when explicitly visited.
    const existingUser = getCurrentUser();
    if (getToken() && existingUser) {
      location.href = existingUser.role === "ADMIN" ? "admin-dashboard.html" : "user-dashboard.html";
      return;
    }

    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const errorBox = document.getElementById("loginError");
      const button = document.getElementById("loginButton");

      errorBox.classList.add("hidden");
      if (!email || !password) {
        errorBox.textContent = "Email and password are required.";
        errorBox.classList.remove("hidden");
        return;
      }

      button.disabled = true;
      button.querySelector(".button-text").textContent = "Signing in…";
      button.querySelector(".spinner").classList.remove("hidden");

      try {
        const response = await API.login(email, password);
        saveSession(response);
        if (response.role === "ADMIN") location.href = "admin-dashboard.html";
        else if (response.role === "USER") location.href = "user-dashboard.html";
        else throw new Error("Unsupported account role.");
      } catch (error) {
        errorBox.textContent = error.message || "Login failed.";
        errorBox.classList.remove("hidden");
      } finally {
        button.disabled = false;
        button.querySelector(".button-text").textContent = "Sign In";
        button.querySelector(".spinner").classList.add("hidden");
      }
    });
  }

  if (document.getElementById("paymentForm") || document.getElementById("transactionsBody")) {
    setupLogout();
    setupMobileMenu();
  }
});

window.saveSession = saveSession;
window.getCurrentUser = getCurrentUser;
window.logout = logout;
window.requireAuth = requireAuth;

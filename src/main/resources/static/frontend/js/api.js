const API_BASE_URL = "";

class ApiError extends Error {
  constructor(status, message, data = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

function getToken() {
  return localStorage.getItem("frm_token");
}

function clearAuthStorage() {
  localStorage.removeItem("frm_token");
  localStorage.removeItem("frm_user");
}

function redirectToLogin() {
  clearAuthStorage();
  if (!location.pathname.endsWith("login.html")) {
    location.href = "login.html";
  }
}

async function apiFetch(path, options = {}) {
  const {
  authenticated = true,
  logoutOn401 = true,
  headers: customHeaders = {},
  ...fetchOptions
  } = options;
  const headers = new Headers(customHeaders);
  if (fetchOptions.body && !(fetchOptions.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (authenticated) {
    const token = getToken();
    if (!token) {
      redirectToLogin();
      throw new ApiError(401, "Authentication required.");
    }
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...fetchOptions,
      headers
    });
  } catch (error) {
    throw new ApiError(0, "Unable to reach the backend. Make sure Spring Boot is running on localhost:8080.");
  }

  let data = null;
  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    try { data = await response.json(); } catch (_) {}
  } else {
    try { data = await response.text(); } catch (_) {}
  }

if (!response.ok) {

    if (response.status === 401) {

        const serverMessage = data && typeof data === "object"
            ? (data.message || data.error || data.detail)
            : null;

        const sessionError =
            data?.code === "SESSION_EXPIRED" ||
            data?.code === "SESSION_INVALID";

        if (sessionError) {
            redirectToLogin();
        }

        const message = sessionError
            ? (serverMessage || "Your session has expired. Please sign in again.")
            : (serverMessage || "Invalid PIN.");

        throw new ApiError(401, message, data);
    }

    if (response.status === 403) {

        const serverMessage = data && typeof data === "object"
            ? (data.message || data.error || data.detail)
            : null;

        if (data?.code === "PAYMENT_BLOCKED") {
            redirectToLogin();
        }

        throw new ApiError(
            403,
            serverMessage || friendlyHttpMessage(403),
            data
        );
    }

    const serverMessage = data && typeof data === "object"
        ? (data.message || data.error || data.detail)
        : null;

    throw new ApiError(
        response.status,
        serverMessage || friendlyHttpMessage(response.status),
        data
    );
}
  return data;
}

function friendlyHttpMessage(status) {
  switch (status) {
    case 400: return "The transaction could not be processed.";
    case 401: return "Authentication failed. Please sign in again.";
    case 403: return "This transaction was blocked or you are not authorized for this action.";
    case 404: return "The requested user or resource was not found.";
    case 409: return "The request conflicts with the current transaction state.";
    default: return "Something went wrong. Please try again.";
  }
}

const API = {
  login: (email, password) => apiFetch("/api/auth/login", {
    method: "POST",
    authenticated: false,
    body: JSON.stringify({ email, password })
  }),
  logout:() => apiFetch("/api/auth/logout", {method: "POST"}),

  makePayment: (payload) => apiFetch("/api/transactions", {
  method: "POST",
  logoutOn401: false,
  body: JSON.stringify(payload)
  }),

  getAdminTransactions: () => apiFetch("/api/admin/transactions", {
    method: "GET"
  }),

  // FUTURE ENDPOINT — NOT CURRENTLY GUARANTEED TO EXIST.
  // Enable/call this only after Spring Boot implements:
  // GET /api/transactions/my
  getMyTransactions: () => apiFetch("/api/transactions/my", {
    method: "GET"
  })
};

window.ApiError = ApiError;
window.apiFetch = apiFetch;
window.API = API;

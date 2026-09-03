let userLatitude = null;
let userLongitude = null;
document.addEventListener("DOMContentLoaded", () => {
  const user = requireAuth("USER");
  if (!user) return;

  setIdentity(user);
  setupPayment();
  getUserLocation();
  setupHistory();
});

function setIdentity(user) {
  const name = user.name || "User";
  const initial = name.charAt(0).toUpperCase();
  ["sidebarName", "welcomeName"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.textContent = name;
  });
  ["sidebarAvatar", "topAvatar"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.textContent = initial;
  });
  const role = document.getElementById("sidebarRole");
  if (role) role.textContent = user.role || "USER";
}

function setupPayment() {
  console.log("SETUP PAYMENT STARTED");
  const form = document.getElementById("paymentForm");
  form.addEventListener("submit", async (event) => {
    console.log("SUBMIT EVENT FIRED");
    event.preventDefault();

    const amount = Number(document.getElementById("amount").value);
    const pin = document.getElementById("pin").value;
    const button = document.getElementById("paymentButton");

    if (!Number.isFinite(amount) || amount <= 0 || !pin) {
      showAlert("Please enter a valid amount, PIN, latitude and longitude.", "error");
      return;
    }
     if (userLatitude === null || userLongitude === null) {
      showAlert("Location is not available. Please allow location access.", "error");
      return;
    }
     


    setButtonLoading(button, true, "Processing…");

    try {
      // Deliberately no userId: Spring identifies the user from the JWT.
      const result = await API.makePayment({amount,pin,latitude: userLatitude,longitude: userLongitude});
      renderPaymentResult(result);
      form.reset(); 
      await loadMyTransactions();// PIN is cleared from the form and is never persisted.
    } catch (error) {
      renderPaymentError(error);
    } finally {
      setButtonLoading(button, false, "Make Payment");
    }
  });
}

function renderPaymentResult(tx) {
  const status = String(tx.status || tx.decision || "UNKNOWN").toUpperCase();
  const normalized = ["SUCCESS", "FAILED", "BLOCKED"].includes(status) ? status : "FAILED";
  const panel = document.getElementById("paymentResult");

  const icon = normalized === "SUCCESS" ? "✓" : normalized === "BLOCKED" ? "!" : "×";
  const title = normalized === "SUCCESS" ? "Payment Successful" :
    normalized === "BLOCKED" ? "Payment Blocked" : "Payment Failed";

  panel.className = `panel result-panel result-${normalized.toLowerCase()}`;
  panel.innerHTML = `
    <div class="result-top">
      <div class="result-title">
        <div class="result-icon">${icon}</div>
        <div><span class="eyebrow">TRANSACTION DECISION</span><h2>${escapeHtml(title)}</h2></div>
      </div>
      <span class="status-badge status-${normalized.toLowerCase()}">${normalized}</span>
    </div>
    <div class="result-grid">
      ${resultItem("Transaction ID", tx.transactionId ?? tx.id ?? "—")}
      ${resultItem("Amount", formatCurrency(tx.amount))}
      ${resultItem("Risk Score", tx.riskScore ?? "—")}
      ${resultItem("Location", formatLocation(tx.latitude, tx.longitude))}
      ${resultItem("Transaction Time", formatDate(tx.createdAt ?? tx.transactionTime ?? tx.timestamp))}
      ${resultItem("Triggered Rules", formatRules(tx.triggeredRules))}
    </div>`;
  panel.classList.remove("hidden");
}

function renderPaymentError(error) {
  const panel = document.getElementById("paymentResult");
  let title = "Payment Failed";
  let message = error.message;

  if (error.status === 403) {
    title = "Payment Blocked / Unauthorized";
    message = "The backend rejected this transaction. The frontend does not override fraud decisions.";
  } else if (error.status === 400) {
    title = "Payment Failed";
    message = error.message || "The backend could not process this payment.";
  } else if (error.status === 404) {
    title = "Resource Not Found";
    message = error.message;
  } else if (error.status === 401) {
    title = "Transaction Failed";
    message = "Invalid Pin. Please enter the correct PIN and try again.";
  }

  panel.className = "panel result-panel result-failed";
  panel.innerHTML = `<div class="result-title"><div class="result-icon">×</div><div><span class="eyebrow">TRANSACTION ERROR</span><h2>${escapeHtml(title)}</h2><p class="muted">${escapeHtml(message)}</p></div></div>`;
  panel.classList.remove("hidden");
}

async function setupHistory() {

  const refresh = document.getElementById("refreshHistory");

  if (refresh) {
    refresh.addEventListener("click", loadMyTransactions);
  }

  await loadMyTransactions();
}
async function loadMyTransactions() {

  const tableWrap = document.getElementById("historyTableWrap");
  const tbody = document.getElementById("historyBody");
  const emptyState = document.getElementById("historyEmpty");

  if (!tableWrap || !tbody || !emptyState) return;

  tableWrap.classList.add("hidden");
  emptyState.classList.add("hidden");

  tbody.innerHTML = `
    <tr>
      <td colspan="5" style="text-align: center;">
        Loading your transactions...
      </td>
    </tr>
  `;

  tableWrap.classList.remove("hidden");

  try {

    const transactions = await API.getMyTransactions();

    renderTransactionHistory(transactions);

  } catch (error) {

    console.error("Transaction history error:", error);

    tableWrap.classList.add("hidden");

    emptyState.textContent = "Unable to load your transactions.";
    emptyState.classList.remove("hidden");
  }
}
function renderTransactionHistory(transactions) {

  const tableWrap = document.getElementById("historyTableWrap");
  const tbody = document.getElementById("historyBody");
  const emptyState = document.getElementById("historyEmpty");

  if (!tableWrap || !tbody || !emptyState) return;

  if (!Array.isArray(transactions) || transactions.length === 0) {

    tableWrap.classList.add("hidden");
    emptyState.classList.remove("hidden");

    return;
  }

  emptyState.classList.add("hidden");
  tableWrap.classList.remove("hidden");

  tbody.innerHTML = transactions.map(tx => {

    const status = String(tx.status || "UNKNOWN").toUpperCase();

    return `
      <tr>

        <td>
          <strong>${escapeHtml(tx.transactionId || "—")}</strong>
        </td>

        <td>
          <strong>${formatCurrency(tx.amount)}</strong>
        </td>

        <td>
          ${escapeHtml(formatLocation(tx.latitude, tx.longitude))}
        </td>

        <td>
          <span class="status-badge status-${status.toLowerCase()}">
            ${escapeHtml(status)}
          </span>
        </td>

        <td>
          ${escapeHtml(formatDate(tx.createdAt))}
        </td>

      </tr>
    `;

  }).join("");
}

  container.innerHTML = transactions.map(tx => {

    const status = String(tx.status || "UNKNOWN").toUpperCase();

    return `
      <div class="transaction-row">

        <div>
          <strong>${escapeHtml(tx.transactionId || "—")}</strong>
          <span>${formatDate(tx.createdAt)}</span>
        </div>

        <div>
          <strong>${formatCurrency(tx.amount)}</strong>
        </div>

        <div>
          <span class="status-badge status-${status.toLowerCase()}">
            ${escapeHtml(status)}
          </span>
        </div>

      </div>
    `;

  }).join("");

function resultItem(label, value) {
  return `<div class="result-item"><span>${escapeHtml(label)}</span><strong>${escapeHtml(String(value ?? "—"))}</strong></div>`;
}
function formatCurrency(value) {
  const n = Number(value);
  return Number.isFinite(n) ? new Intl.NumberFormat("en-IN", {style:"currency", currency:"INR", maximumFractionDigits:2}).format(n) : "—";
}
function formatLocation(lat, lng) {
  if (lat == null || lng == null) return "—";
  return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`;
}
function formatDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString("en-IN", {dateStyle:"medium", timeStyle:"short"});
}
function formatRules(rules) {
  if (Array.isArray(rules)) return rules.length ? rules.join(", ") : "None";
  return rules || "None";
}
function setButtonLoading(button, loading, text) {
  button.disabled = loading;
  button.querySelector(".button-text").textContent = text;
  button.querySelector(".spinner").classList.toggle("hidden", !loading);
}
function showAlert(message, type) {
  const box = document.getElementById("globalAlert");
  box.textContent = message;
  box.className = `alert alert-${type}`;
  setTimeout(() => box.classList.add("hidden"), 4500);
}
function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[ch]));
}
 

function getUserLocation() {
    const locationStatus = document.getElementById("locationStatus");

    if (!navigator.geolocation) {
        locationStatus.textContent = "❌ Location is not supported by this browser.";
        return;
    }

    navigator.geolocation.getCurrentPosition(
        (position) => {
            userLatitude = position.coords.latitude;
            userLongitude = position.coords.longitude;

            locationStatus.textContent = "📍 Location detected";
        },
        (error) => {
            userLatitude = null;
            userLongitude = null;

            locationStatus.textContent =
                "❌ Location permission is required to make a payment.";
        }
    );
}

 
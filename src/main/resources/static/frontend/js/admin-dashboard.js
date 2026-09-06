let allTransactions = [];
let currentRiskSort = "NONE";
let currentDateSort = "DESC";

document.addEventListener("DOMContentLoaded", async () => {
  const user = requireAuth("ADMIN");
  if (!user) return;

  setAdminIdentity(user);
  bindControls();
  await loadTransactions();
   
});

function setAdminIdentity(user) {
  const name = user.name || "Admin";
  const initial = name.charAt(0).toUpperCase();
  document.getElementById("sidebarName").textContent = name;
  document.getElementById("sidebarRole").textContent = user.role || "ADMIN";
  document.getElementById("sidebarAvatar").textContent = initial;
  document.getElementById("topAvatar").textContent = initial;
}
 

function bindControls() {
  document.getElementById("searchInput").addEventListener("input", renderFiltered);
  document.getElementById("statusFilter").addEventListener("change", renderFiltered);
  document.getElementById("riskSort").addEventListener("change", e => {
    currentRiskSort = e.target.value;
    renderFiltered();
  });
  document.getElementById("dateSort").addEventListener("change", e => {
    currentDateSort = e.target.value;
    renderFiltered();
  });
  document.getElementById("refreshTransactions").addEventListener("click", loadTransactions);
}

async function loadTransactions() {
  setLoading(true);
  hideAdminAlert();
  try {
    const response = await API.getAdminTransactions();
    allTransactions = Array.isArray(response) ? response : (response?.transactions || []);
    updateStats(allTransactions);
    renderFiltered();
  } catch (error) {
    allTransactions = [];
    updateStats([]);
    showAdminAlert(error.message || "Could not load transactions.", "error");
    renderFiltered();
  } finally {
    setLoading(false);
  }
}

function updateStats(list) {
  const status = tx => String(tx.status || tx.decision || "").toUpperCase();
  document.getElementById("totalCount").textContent = list.length;
  document.getElementById("successCount").textContent = list.filter(tx => status(tx) === "SUCCESS").length;
  document.getElementById("failedCount").textContent = list.filter(tx => status(tx) === "FAILED").length;
  document.getElementById("blockedCount").textContent = list.filter(tx => status(tx) === "BLOCKED").length;
  // High risk is a display metric only; it does NOT make or change a backend decision.
  document.getElementById("highRiskCount").textContent = list.filter(tx => Number(tx.riskScore) >= 75).length;
}

function renderFiltered() {
  const query = document.getElementById("searchInput").value.trim().toLowerCase();
  const statusFilter = document.getElementById("statusFilter").value;

  let list = allTransactions.filter(tx => {
    const status = String(tx.status || tx.decision || "").toUpperCase();
    const searchable = [
      tx.transactionId, tx.userId, tx.userName, tx.userEmail, tx.amount,
      tx.latitude, tx.longitude, tx.riskScore, tx.triggeredRules, tx.status, tx.createdAt
    ].join(" ").toLowerCase();

    return (!query || searchable.includes(query)) &&
           (statusFilter === "ALL" || status === statusFilter);
  });

  if (currentRiskSort !== "NONE") {
    list.sort((a,b) => {
      const diff = Number(a.riskScore || 0) - Number(b.riskScore || 0);
      return currentRiskSort === "ASC" ? diff : -diff;
    });
  } else {
    list.sort((a,b) => {
      const da = new Date(a.createdAt || a.transactionTime || a.timestamp).getTime() || 0;
      const db = new Date(b.createdAt || b.transactionTime || b.timestamp).getTime() || 0;
      return currentDateSort === "ASC" ? da - db : db - da;
    });
  }

  renderTable(list);
}

function renderTable(list) {
  const body = document.getElementById("transactionsBody");
  body.innerHTML = "";

  document.getElementById("tableEmpty").classList.toggle("hidden", list.length !== 0);
  document.getElementById("transactionsTableWrap").classList.toggle("hidden", list.length === 0);

  list.forEach(tx => {
    const status = String(tx.status || tx.decision || "UNKNOWN").toUpperCase();
    const risk = Number(tx.riskScore);
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>${escapeHtml(tx.transactionId ?? tx.id ?? "—")}</strong></td>
      <td>${escapeHtml(tx.userName ?? tx.userId ?? "—")}</td>
      <td>${escapeHtml(tx.userEmail ?? "—")}</td>
      <td class="amount">${escapeHtml(formatCurrency(tx.amount))}</td>
      <td class="location">${escapeHtml(formatLocation(tx.latitude, tx.longitude))}</td>
      <td><span class="risk-score ${risk >= 100 ? "risk-high" : risk >= 75 ? "risk-med" : "risk-low"}">${escapeHtml(tx.riskScore ?? "—")}</span></td>
      <td><div class="rules">${renderRules(tx.triggeredRules)}</div></td>
      <td><span class="status-badge status-${status.toLowerCase()}">${escapeHtml(status)}</span></td>
      <td class="muted">${escapeHtml(formatDate(tx.createdAt ?? tx.transactionTime ?? tx.timestamp))}</td>`;
    body.appendChild(tr);
  });
}

function renderRules(rules) {
  if (!Array.isArray(rules)) return rules ? `<span class="rule-tag">${escapeHtml(rules)}</span>` : `<span class="muted">None</span>`;
  if (!rules.length) return `<span class="muted">None</span>`;
  return rules.map(rule => `<span class="rule-tag">${escapeHtml(rule)}</span>`).join("");
}

function setLoading(loading) {
  document.getElementById("tableLoading").classList.toggle("hidden", !loading);
  if (loading) {
    document.getElementById("transactionsTableWrap").classList.add("hidden");
    document.getElementById("tableEmpty").classList.add("hidden");
  }
  const btn = document.getElementById("refreshTransactions");
  btn.disabled = loading;
}

function showAdminAlert(message, type) {
  const box = document.getElementById("adminAlert");
  box.textContent = message;
  box.className = `alert alert-${type}`;
}
function hideAdminAlert() {
  document.getElementById("adminAlert").classList.add("hidden");
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
function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[ch]));
}

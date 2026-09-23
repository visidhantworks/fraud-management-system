let allTransactions = [];

let currentRiskSort = "NONE";

let currentDateSort = "DESC";

let selectedBehaviorTransaction = null;


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

  document.getElementById("sidebarRole").textContent =
    user.role || "ADMIN";

  document.getElementById("sidebarAvatar").textContent = initial;

  document.getElementById("topAvatar").textContent = initial;

}


function bindControls() {

  document
    .getElementById("searchInput")
    .addEventListener("input", renderFiltered);


  document
    .getElementById("statusFilter")
    .addEventListener("change", renderFiltered);


  document
    .getElementById("riskSort")
    .addEventListener("change", e => {

      currentRiskSort = e.target.value;

      renderFiltered();

    });


  document
    .getElementById("dateSort")
    .addEventListener("change", e => {

      currentDateSort = e.target.value;

      renderFiltered();

    });


  document
    .getElementById("refreshTransactions")
    .addEventListener("click", loadTransactions);


  /*
   * Behavior modal controls
   */

  document
    .getElementById("behaviorModalClose")
    .addEventListener("click", closeBehaviorModal);


  document
    .getElementById("behaviorModalCloseButton")
    .addEventListener("click", closeBehaviorModal);


  document
    .getElementById("behaviorModalOverlay")
    .addEventListener("click", event => {

      if (event.target.id === "behaviorModalOverlay") {

        closeBehaviorModal();

      }

    });


  document.addEventListener("keydown", event => {

    if (
      event.key === "Escape" &&
      !document
        .getElementById("behaviorModalOverlay")
        .classList.contains("hidden")
    ) {

      closeBehaviorModal();

    }

  });

}


async function loadTransactions() {

  setLoading(true);

  hideAdminAlert();

  try {

    const response = await API.getAdminTransactions();

    allTransactions =
      Array.isArray(response)
        ? response
        : (response?.transactions || []);

    updateStats(allTransactions);

    renderFiltered();

  } catch (error) {

    allTransactions = [];

    updateStats([]);

    showAdminAlert(
      error.message || "Could not load transactions.",
      "error"
    );

    renderFiltered();

  } finally {

    setLoading(false);

  }

}


function updateStats(list) {

  const status = tx =>
    String(
      tx.status ||
      tx.decision ||
      ""
    ).toUpperCase();


  document.getElementById("totalCount").textContent =
    list.length;


  document.getElementById("successCount").textContent =
    list.filter(
      tx => status(tx) === "SUCCESS"
    ).length;


  document.getElementById("failedCount").textContent =
    list.filter(
      tx => status(tx) === "FAILED"
    ).length;


  document.getElementById("blockedCount").textContent =
    list.filter(
      tx => status(tx) === "BLOCKED"
    ).length;


  /*
   * Display metric only.
   * Does not modify backend decision logic.
   */

  document.getElementById("highRiskCount").textContent =
    list.filter(
      tx => Number(tx.riskScore) >= 75
    ).length;

}


function renderFiltered() {

  const query =
    document
      .getElementById("searchInput")
      .value
      .trim()
      .toLowerCase();


  const statusFilter =
    document.getElementById("statusFilter").value;


  let list = allTransactions.filter(tx => {

    const status =
      String(
        tx.status ||
        tx.decision ||
        ""
      ).toUpperCase();


    const searchable = [

      tx.transactionId,
      tx.id,

      tx.userId,
      tx.userName,
      tx.userEmail,

      tx.amount,

      tx.latitude,
      tx.longitude,

      tx.riskScore,

      tx.triggeredRules,

      tx.status,

      tx.createdAt,

      tx.transactionTime,

      tx.timestamp,

      getBehaviorCategory(tx)

    ]
      .join(" ")
      .toLowerCase();


    return (

      (!query || searchable.includes(query)) &&

      (
        statusFilter === "ALL" ||
        status === statusFilter
      )

    );

  });


  if (currentRiskSort !== "NONE") {

    list.sort((a, b) => {

      const diff =
        Number(a.riskScore || 0) -
        Number(b.riskScore || 0);


      return currentRiskSort === "ASC"
        ? diff
        : -diff;

    });

  } else {

    list.sort((a, b) => {

      const da =
        new Date(
          a.createdAt ||
          a.transactionTime ||
          a.timestamp
        ).getTime() || 0;


      const db =
        new Date(
          b.createdAt ||
          b.transactionTime ||
          b.timestamp
        ).getTime() || 0;


      return currentDateSort === "ASC"
        ? da - db
        : db - da;

    });

  }


  renderTable(list);

}


function renderTable(list) {

  const body =
    document.getElementById("transactionsBody");


  body.innerHTML = "";


  document
    .getElementById("tableEmpty")
    .classList
    .toggle(
      "hidden",
      list.length !== 0
    );


  document
    .getElementById("transactionsTableWrap")
    .classList
    .toggle(
      "hidden",
      list.length === 0
    );


  list.forEach(tx => {

    const status =
      String(
        tx.status ||
        tx.decision ||
        "UNKNOWN"
      ).toUpperCase();


    const risk =
      Number(tx.riskScore);


    const transactionId =
      tx.transactionId ??
      tx.id ??
      "—";


    const behaviorCategory =
      getBehaviorCategory(tx);


    const tr =
      document.createElement("tr");


    tr.innerHTML = `

      <td>
        <strong>
          ${escapeHtml(transactionId)}
        </strong>
      </td>


      <td>
        ${escapeHtml(
          tx.userName ??
          tx.userId ??
          "—"
        )}
      </td>


      <td>
        ${escapeHtml(
          tx.userEmail ??
          "—"
        )}
      </td>


      <td class="amount">
        ${escapeHtml(
          formatCurrency(tx.amount)
        )}
      </td>


      <td class="location">
        ${escapeHtml(
          formatLocation(
            tx.latitude,
            tx.longitude
          )
        )}
      </td>


      <td>
        <span
          class="risk-score ${
            risk >= 100
              ? "risk-high"
              : risk >= 75
                ? "risk-med"
                : "risk-low"
          }"
        >
          ${escapeHtml(
            tx.riskScore ?? "—"
          )}
        </span>
      </td>


      <td>
        <div class="rules">
          ${renderRules(
            tx.triggeredRules
          )}
        </div>
      </td>


      <td>
        ${renderBehaviorButton(tx)}
      </td>


      <td>
        <span
          class="status-badge status-${status.toLowerCase()}"
        >
          ${escapeHtml(status)}
        </span>
      </td>


      <td class="muted">
        ${escapeHtml(
          formatDate(
            tx.createdAt ??
            tx.transactionTime ??
            tx.timestamp
          )
        )}
      </td>

    `;


    body.appendChild(tr);

  });

}


function renderBehaviorButton(tx) {

  const category =
    getBehaviorCategory(tx);


  /*
   * If backend does not provide behavioral
   * analysis, do not fabricate a category.
   */

  if (!category) {

    return `
      <span class="behavior-button behavior-unavailable">
        Not available
      </span>
    `;

  }


  const normalized =
    normalizeBehaviorCategory(category);


  const cssClass =
    normalized === "NORMAL"
      ? "behavior-normal"
      : normalized === "SLIGHTLY_ABNORMAL"
        ? "behavior-slightly-abnormal"
        : normalized === "HIGHLY_ABNORMAL"
          ? "behavior-highly-abnormal"
          : "behavior-unavailable";


  const label =
    formatBehaviorCategory(category);


  /*
   * Store the transaction in memory instead
   * of putting the whole object into HTML.
   */

  const button =
    document.createElement("button");


  button.type = "button";

  button.className =
    `behavior-button ${cssClass}`;


  button.textContent = label;


  button.addEventListener(
    "click",
    () => openBehaviorModal(tx)
  );


  /*
   * Return DOM node through wrapper.
   * renderTable inserts HTML first, so this
   * function is converted to an HTML marker
   * and replaced after the row is inserted.
   */

  return `
    <button
      type="button"
      class="behavior-button ${cssClass}"
      data-behavior-transaction-id="${escapeHtml(
        String(
          tx.transactionId ??
          tx.id ??
          ""
        )
      )}"
    >
      ${escapeHtml(label)}
    </button>
  `;

}


function renderRules(rules) {

  if (!Array.isArray(rules)) {

    return rules
      ? `<span class="rule-tag">
           ${escapeHtml(rules)}
         </span>`
      : `<span class="muted">None</span>`;

  }


  if (!rules.length) {

    return `<span class="muted">None</span>`;

  }


  return rules
    .map(
      rule =>
        `<span class="rule-tag">
          ${escapeHtml(rule)}
        </span>`
    )
    .join("");

}


function getBehaviorObject(tx) {

  /*
   * Supports the most likely backend response
   * structures without creating any values.
   */

  return (
    tx.pythonBehavior ??
    tx.behaviorAnalysis ??
    tx.behavior ??
    tx.pythonBehaviorAnalysis ??
    tx
  );

}


function getBehaviorCategory(tx) {

  const behavior =
    getBehaviorObject(tx);


  return (
    behavior.behaviorCategory ??
    behavior.behavior_category ??
    behavior.category ??
    null
  );

}


function getBehavioralRisk(tx) {

  const behavior =
    getBehaviorObject(tx);


  return (
    behavior.behavioralRisk ??
    behavior.behavioral_risk ??
    behavior.pythonBehavioralRisk ??
    null
  );

}


function getBehaviorMetric(tx, ...keys) {

  const behavior =
    getBehaviorObject(tx);


  for (const key of keys) {

    if (
      behavior[key] !== undefined &&
      behavior[key] !== null
    ) {

      return behavior[key];

    }

  }


  return null;

}


function openBehaviorModal(tx) {

  selectedBehaviorTransaction = tx;


  const transactionId =
    tx.transactionId ??
    tx.id ??
    "—";


  const category =
    getBehaviorCategory(tx);


  const behavioralRisk =
    getBehavioralRisk(tx);


  document.getElementById(
    "behaviorTransactionId"
  ).textContent =
    `Transaction: ${transactionId}`;


  setBehaviorCategory(category);


  document.getElementById(
    "behavioralRisk"
  ).textContent =
    formatNumber(
      behavioralRisk
    );


  /*
   * Transaction behavior
   */

  document.getElementById(
    "behaviorAverageAmount"
  ).textContent =
    formatCurrencyOrDash(
      getBehaviorMetric(
        tx,
        "averageAmount",
        "average_amount"
      )
    );


  document.getElementById(
    "behaviorMaximumAmount"
  ).textContent =
    formatCurrencyOrDash(
      getBehaviorMetric(
        tx,
        "maximumAmount",
        "maximum_amount",
        "maxAmount",
        "max_amount"
      )
    );


  document.getElementById(
    "behaviorMinimumAmount"
  ).textContent =
    formatCurrencyOrDash(
      getBehaviorMetric(
        tx,
        "minimumAmount",
        "minimum_amount",
        "minAmount",
        "min_amount"
      )
    );


  document.getElementById(
    "behaviorTransactionCount"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "transactionCount",
        "transaction_count"
      )
    );


  document.getElementById(
    "behaviorSuccessfulTransactions"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "successfulTransactions",
        "successful_transactions"
      )
    );


  document.getElementById(
    "behaviorFailedTransactions"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "failedTransactions",
        "failed_transactions"
      )
    );


  document.getElementById(
    "behaviorFailedTransactionRatio"
  ).textContent =
    formatPercentage(
      getBehaviorMetric(
        tx,
        "failedTransactionRatio",
        "failed_transaction_ratio"
      )
    );


  /*
   * Risk components
   */

  document.getElementById(
    "behaviorAmountDeviation"
  ).textContent =
    formatPercentage(
      getBehaviorMetric(
        tx,
        "amountDeviationPercentage",
        "amount_deviation_percentage",
        "amountDeviation",
        "amount_deviation"
      )
    );


  document.getElementById(
    "behaviorAmountRisk"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "amountBehaviorRisk",
        "amount_behavior_risk"
      )
    );


  document.getElementById(
    "behaviorFailureRisk"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "failureBehaviorRisk",
        "failure_behavior_risk"
      )
    );


  document.getElementById(
    "behaviorFrequencyRisk"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "frequencyBehaviorRisk",
        "frequency_behavior_risk"
      )
    );


  document.getElementById(
    "behaviorLocationRisk"
  ).textContent =
    formatNumber(
      getBehaviorMetric(
        tx,
        "locationBehaviorRisk",
        "location_behavior_risk"
      )
    );


  /*
   * Open modal without reloading dashboard.
   */

  document
    .getElementById("behaviorModalOverlay")
    .classList
    .remove("hidden");


  document.body.style.overflow = "hidden";

}


function closeBehaviorModal() {

  selectedBehaviorTransaction = null;


  document
    .getElementById("behaviorModalOverlay")
    .classList
    .add("hidden");


  document.body.style.overflow = "";

}


function setBehaviorCategory(category) {

  const element =
    document.getElementById(
      "behaviorCategory"
    );


  if (!category) {

    element.textContent =
      "Not available";

    element.className =
      "behavior-category-value";

    return;

  }


  const normalized =
    normalizeBehaviorCategory(
      category
    );


  element.textContent =
    formatBehaviorCategory(
      category
    );


  element.className =
    `behavior-category-value ${
      normalized === "NORMAL"
        ? "behavior-normal"
        : normalized === "SLIGHTLY_ABNORMAL"
          ? "behavior-slightly-abnormal"
          : normalized === "HIGHLY_ABNORMAL"
            ? "behavior-highly-abnormal"
            : ""
    }`;

}


function normalizeBehaviorCategory(value) {

  return String(value || "")
    .trim()
    .toUpperCase()
    .replace(/[\s-]+/g, "_");

}


function formatBehaviorCategory(value) {

  const normalized =
    normalizeBehaviorCategory(
      value
    );


  switch (normalized) {

    case "NORMAL":
      return "NORMAL";

    case "SLIGHTLY_ABNORMAL":
      return "SLIGHTLY ABNORMAL";

    case "HIGHLY_ABNORMAL":
      return "HIGHLY ABNORMAL";

    default:
      return String(value);

  }

}


function formatNumber(value) {

  if (
    value === null ||
    value === undefined ||
    value === ""
  ) {

    return "—";

  }


  const number =
    Number(value);


  return Number.isFinite(number)
    ? number.toLocaleString("en-IN", {
        maximumFractionDigits: 2
      })
    : String(value);

}


function formatPercentage(value) {

  if (
    value === null ||
    value === undefined ||
    value === ""
  ) {

    return "—";

  }


  const number =
    Number(value);


  if (!Number.isFinite(number)) {

    return String(value);

  }


  return `${number.toLocaleString(
    "en-IN",
    {
      maximumFractionDigits: 2
    }
  )}%`;

}


function formatCurrencyOrDash(value) {

  if (
    value === null ||
    value === undefined ||
    value === ""
  ) {

    return "—";

  }


  return formatCurrency(value);

}


function setLoading(loading) {

  document
    .getElementById("tableLoading")
    .classList
    .toggle(
      "hidden",
      !loading
    );


  if (loading) {

    document
      .getElementById("transactionsTableWrap")
      .classList
      .add("hidden");


    document
      .getElementById("tableEmpty")
      .classList
      .add("hidden");

  }


  const btn =
    document.getElementById(
      "refreshTransactions"
    );


  btn.disabled = loading;

}


function showAdminAlert(message, type) {

  const box =
    document.getElementById(
      "adminAlert"
    );


  box.textContent = message;


  box.className =
    `alert alert-${type}`;

}


function hideAdminAlert() {

  document
    .getElementById("adminAlert")
    .classList
    .add("hidden");

}


function formatCurrency(value) {

  const n =
    Number(value);


  return Number.isFinite(n)
    ? new Intl.NumberFormat(
        "en-IN",
        {
          style: "currency",
          currency: "INR",
          maximumFractionDigits: 2
        }
      ).format(n)
    : "—";

}


function formatLocation(lat, lng) {

  if (
    lat == null ||
    lng == null
  ) {

    return "—";

  }


  return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`;

}


function formatDate(value) {

  if (!value) {

    return "—";

  }


  const date =
    new Date(value);


  return Number.isNaN(
    date.getTime()
  )
    ? String(value)
    : date.toLocaleString(
        "en-IN",
        {
          dateStyle: "medium",
          timeStyle: "short"
        }
      );

}


function escapeHtml(value) {

  return String(value).replace(
    /[&<>"']/g,
    ch => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;"
    }[ch])
  );

}


/*
 * IMPORTANT:
 *
 * Because the Behavior button is generated dynamically,
 * we use event delegation on the table body.
 *
 * This also means the button keeps working after:
 * - searching
 * - sorting
 * - filtering
 * - refreshing
 */

document.addEventListener("click", event => {

  const button =
    event.target.closest(
      "[data-behavior-transaction-id]"
    );


  if (!button) return;


  const transactionId =
    button.dataset.behaviorTransactionId;


  const transaction =
    allTransactions.find(
      tx =>
        String(
          tx.transactionId ??
          tx.id ??
          ""
        ) === String(transactionId)
    );


  if (!transaction) {

    showAdminAlert(
      "Could not find the selected transaction.",
      "error"
    );

    return;

  }


  openBehaviorModal(transaction);

});
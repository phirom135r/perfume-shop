// static/js/dashboard.js
let salesChart = null;

document.addEventListener("DOMContentLoaded", async () => {
    bindRangeUI();
    updateDateInputsByRange();   // ✅ auto fill on load
    await refreshDashboard();
});

function bindRangeUI() {
    const rangeSelect = document.getElementById("rangeSelect");
    const btnApply = document.getElementById("btnApplyRange");

    if (!rangeSelect || !btnApply) return;

    rangeSelect.addEventListener("change", () => {
        updateDateInputsByRange();
    });

    btnApply.addEventListener("click", async () => {
        await refreshDashboard();
    });
}

/* ======================================================
   ✅ AUTO FILL FROM/TO BASED ON RANGE
   ====================================================== */
function formatDateInput(d) {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`; // <input type="date"> needs yyyy-MM-dd
}

function updateDateInputsByRange() {
    const range = document.getElementById("rangeSelect")?.value || "LAST_7_DAYS";
    const fromInput = document.getElementById("fromDate");
    const toInput = document.getElementById("toDate");

    if (!fromInput || !toInput) return;

    const today = new Date();
    let fromDate = new Date(today);
    let toDate = new Date(today);

    if (range === "TODAY") {
        // from = today
    } else if (range === "LAST_7_DAYS") {
        fromDate = new Date(today);
        fromDate.setDate(today.getDate() - 6); // inclusive 7 days
    } else if (range === "THIS_MONTH") {
        fromDate = new Date(today.getFullYear(), today.getMonth(), 1);
    } else if (range === "CUSTOM") {
        fromInput.disabled = false;
        toInput.disabled = false;
        return;
    }

    fromInput.value = formatDateInput(fromDate);
    toInput.value = formatDateInput(toDate);

    // lock inputs unless custom
    fromInput.disabled = true;
    toInput.disabled = true;
}

/* ======================================================
   API QUERY PARAMS
   ====================================================== */
function getQueryParams() {
    const range = document.getElementById("rangeSelect")?.value || "LAST_7_DAYS";
    const from = document.getElementById("fromDate")?.value || "";
    const to = document.getElementById("toDate")?.value || "";

    const qs = new URLSearchParams();
    qs.set("range", range);

    // ✅ Always send from/to (even non-custom) for backend clarity
    if (from) qs.set("from", from);
    if (to) qs.set("to", to);

    return qs.toString();
}

async function refreshDashboard() {
    const qs = getQueryParams();
    await loadSummary(qs);
    await loadSales(qs);
    await loadTopProducts(qs);
}

function money2(n) {
    return Number(n || 0).toFixed(2);
}

function fmtDateTime(v) {
    if (!v) return "-";
    return String(v).replace("T", " ").substring(0, 16);
}

/* ======================================================
   SUMMARY (Cards + Recent Orders)
   ====================================================== */
async function loadSummary(qs) {
    const res = await fetch(`/admin/api/dashboard/summary?${qs}`, { headers: { "Accept": "application/json" } });
    if (!res.ok) return;

    const d = await res.json();

    // ✅ Cards (use same data for both sales boxes)
    document.getElementById("salesAmount").textContent = money2(d.sales);
    document.getElementById("salesOrders").textContent = d.orders ?? 0;

    document.getElementById("salesAmount2").textContent = money2(d.sales);
    document.getElementById("salesOrders2").textContent = d.orders ?? 0;

    document.getElementById("pendingCount").textContent = d.pendingCount ?? 0;
    document.getElementById("paidCount").textContent = d.paidCount ?? 0;
    document.getElementById("cancelledCount").textContent = d.cancelledCount ?? 0;

    // Titles
    const label = d.rangeLabel || rangeLabelFromInputs();
    const salesTitle = document.getElementById("salesTitle");
    const topTitle = document.getElementById("topTitle");
    if (salesTitle) salesTitle.textContent = `Sales (${label})`;
    if (topTitle) topTitle.textContent = `Top Products (${label})`;

    // Recent Orders
    const body = document.getElementById("recentOrdersBody");
    body.innerHTML = "";

    const rows = d.recentOrders || [];
    if (rows.length === 0) {
        body.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-3">No recent orders</td></tr>`;
        return;
    }

    rows.forEach(o => {
        const st = String(o.status || "").toUpperCase();
        const badge =
            st === "PAID" ? "text-bg-success"
                : st === "PENDING" ? "text-bg-warning"
                    : "text-bg-danger";

        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td class="fw-semibold">${escapeHtml(o.invoice || "-")}</td>
          <td>${escapeHtml(o.customerName || "-")}</td>
          <td class="text-end">$${money2(o.total)}</td>
          <td><span class="badge ${badge}">${escapeHtml(st || "-")}</span></td>
          <td>${fmtDateTime(o.createdAt)}</td>
          <td class="text-center">
            <a class="btn btn-sm btn-outline-primary" href="/admin/orders">Open</a>
          </td>
        `;
        body.appendChild(tr);
    });
}

function rangeLabelFromInputs() {
    const from = document.getElementById("fromDate")?.value || "";
    const to = document.getElementById("toDate")?.value || "";
    if (!from || !to) return "Selected Range";
    // yyyy-MM-dd => MM/dd/yyyy
    return `${prettyMMDDYYYY(from)} - ${prettyMMDDYYYY(to)}`;
}

function prettyMMDDYYYY(ymd) {
    const [y, m, d] = String(ymd).split("-");
    if (!y || !m || !d) return ymd;
    return `${m}/${d}/${y}`;
}

/* ======================================================
   SALES CHART
   ====================================================== */
async function loadSales(qs) {
    const res = await fetch(`/admin/api/dashboard/sales?${qs}`, { headers: { "Accept": "application/json" } });
    if (!res.ok) return;

    const rows = await res.json();
    const labels = rows.map(r => (r.date || "").substring(5)); // MM-dd
    const values = rows.map(r => Number(r.total || 0));

    const canvas = document.getElementById("salesChart");
    if (!canvas) return;

    if (salesChart) salesChart.destroy();

    salesChart = new Chart(canvas, {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: "Sales",
                data: values,
                tension: 0.35
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: true } },
            scales: { y: { beginAtZero: true } }
        }
    });
}

/* ======================================================
   TOP PRODUCTS
   ====================================================== */
async function loadTopProducts(qs) {
    const res = await fetch(`/admin/api/dashboard/top-products?${qs}`, { headers: { "Accept": "application/json" } });
    if (!res.ok) return;

    const rows = await res.json();
    const body = document.getElementById("topProductsBody");
    body.innerHTML = "";

    if (!rows || rows.length === 0) {
        body.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-3">No data</td></tr>`;
        return;
    }

    rows.forEach(r => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${escapeHtml(r.name || "-")}</td>
          <td class="text-center">${Number(r.qty || 0)}</td>
          <td class="text-end">$${money2(r.total)}</td>
        `;
        body.appendChild(tr);
    });
}

/* ======================================================
   UTIL
   ====================================================== */
function escapeHtml(s) {
    return String(s || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
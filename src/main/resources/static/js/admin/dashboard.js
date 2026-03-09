// =====================================================
// Dashboard JS - Clean & Safe Version
// =====================================================

let salesChart = null;

document.addEventListener("DOMContentLoaded", async () => {
    bindRangeUI();
    updateDateInputsByRange();
    await refreshDashboard();
});

// =====================================================
// RANGE FILTER
// =====================================================

function bindRangeUI() {
    const rangeSelect = document.getElementById("rangeSelect");
    const btnApply = document.getElementById("btnApplyRange");

    if (rangeSelect) {
        rangeSelect.addEventListener("change", updateDateInputsByRange);
    }

    if (btnApply) {
        btnApply.addEventListener("click", async () => {
            await refreshDashboard();
        });
    }
}

function formatDateInput(d) {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
}

function updateDateInputsByRange() {
    const range = document.getElementById("rangeSelect")?.value || "LAST_7_DAYS";
    const fromInput = document.getElementById("fromDate");
    const toInput = document.getElementById("toDate");

    if (!fromInput || !toInput) return;

    const today = new Date();
    let fromDate = new Date(today);
    let toDate = new Date(today);

    if (range === "LAST_7_DAYS") {
        fromDate.setDate(today.getDate() - 6);
    } else if (range === "THIS_MONTH") {
        fromDate = new Date(today.getFullYear(), today.getMonth(), 1);
    } else if (range === "CUSTOM") {
        fromInput.disabled = false;
        toInput.disabled = false;
        return;
    }

    fromInput.value = formatDateInput(fromDate);
    toInput.value = formatDateInput(toDate);

    fromInput.disabled = true;
    toInput.disabled = true;
}

function getQueryParams() {
    const range = document.getElementById("rangeSelect")?.value || "LAST_7_DAYS";
    const from = document.getElementById("fromDate")?.value || "";
    const to = document.getElementById("toDate")?.value || "";

    const qs = new URLSearchParams();
    qs.set("range", range);
    if (from) qs.set("from", from);
    if (to) qs.set("to", to);

    return qs.toString();
}

// =====================================================
// MAIN REFRESH
// =====================================================

async function refreshDashboard() {
    const qs = getQueryParams();
    await loadSummary(qs);
    await loadSales(qs);
    await loadTopProducts(qs);
}

// =====================================================
// UTIL
// =====================================================

function money2(n) {
    return Number(n || 0).toFixed(2);
}

function fmtDateTime(v) {
    if (!v) return "-";
    return String(v).replace("T", " ").substring(0, 16);
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function escapeHtml(s) {
    return String(s || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

// =====================================================
// SUMMARY (Cards + Recent Orders)
// =====================================================

async function loadSummary(qs) {
    try {
        const res = await fetch(`/admin/api/dashboard/summary?${qs}`);
        if (!res.ok) return;

        const d = await res.json();

        // Sales
        setText("salesAmount", money2(d.sales));
        setText("salesOrders", d.orders ?? 0);

        // Order status
        setText("pendingCount", d.pendingCount ?? 0);
        setText("paidCount", d.paidCount ?? 0);
        setText("cancelledCount", d.cancelledCount ?? 0);

        // Inventory KPIs
        setText("totalProducts", d.totalProducts ?? 0);
        setText("totalStockUnits", d.totalStockUnits ?? 0);
        setText("lowStockThreshold", d.lowStockThreshold ?? 5);
        setText("lowStockCount", d.lowStockCount ?? 0);
        setText("outOfStockCount", d.outOfStockCount ?? 0);

        // Titles
        const label = d.rangeLabel || "Selected Range";
        setText("salesTitle", `Sales (${label})`);
        setText("topTitle", `Top Products (${label})`);

        // Recent Orders
        const body = document.getElementById("recentOrdersBody");
        if (!body) return;

        body.innerHTML = "";

        const rows = d.recentOrders || [];
        if (rows.length === 0) {
            body.innerHTML =
                `<tr><td colspan="6" class="text-center text-muted py-3">No recent orders</td></tr>`;
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
                <td class="fw-semibold">${escapeHtml(o.invoice)}</td>
                <td>${escapeHtml(o.customerName)}</td>
                <td >$${money2(o.total)}</td>
                <td><span class="badge ${badge}">${escapeHtml(st)}</span></td>
                <td>${fmtDateTime(o.createdAt)}</td>
               
            `;
            body.appendChild(tr);
        });

    } catch (err) {
        console.error("Dashboard summary error:", err);
    }
}

// =====================================================
// SALES CHART
// =====================================================

async function loadSales(qs) {
    try {
        const res = await fetch(`/admin/api/dashboard/sales?${qs}`);
        if (!res.ok) return;

        const rows = await res.json();
        const labels = rows.map(r => (r.date || "").substring(5));
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

    } catch (err) {
        console.error("Sales chart error:", err);
    }
}

// =====================================================
// TOP PRODUCTS
// =====================================================

async function loadTopProducts(qs) {
    try {
        const res = await fetch(`/admin/api/dashboard/top-products?${qs}`);
        if (!res.ok) return;

        const rows = await res.json();
        const body = document.getElementById("topProductsBody");
        if (!body) return;

        body.innerHTML = "";

        if (!rows || rows.length === 0) {
            body.innerHTML =
                `<tr><td colspan="3" class="text-center text-muted py-3">No data</td></tr>`;
            return;
        }

        rows.forEach(r => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${escapeHtml(r.name)}</td>
                <td class="text-center">${Number(r.qty || 0)}</td>
                <td class="text-end" >$${money2(r.total)}</td>
            `;
            body.appendChild(tr);
        });

    } catch (err) {
        console.error("Top products error:", err);
    }
}
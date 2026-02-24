let orderModal;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("orderDetailModal");
    if (modalEl && window.bootstrap) {
        orderModal = new bootstrap.Modal(modalEl);
    }

    document.querySelectorAll(".btnViewOrder").forEach((btn) => {
        btn.addEventListener("click", async () => {
            const id = btn.getAttribute("data-id");
            if (!id) return;
            await openOrderDetail(id);
        });
    });
});

function money2(n) {
    return Number(n || 0).toFixed(2);
}

function fmtDate(v) {
    if (!v) return "-";
    // LocalDateTime usually: "2026-02-24T12:34:56"
    const s = String(v).replace("T", " ");
    return s.length >= 16 ? s.substring(0, 16) : s;
}

async function openOrderDetail(id) {
    try {
        const res = await fetch(`/admin/api/orders/${encodeURIComponent(id)}`, {
            headers: { Accept: "application/json" },
        });

        if (!res.ok) {
            Swal.fire("Error", "Cannot load order details", "error");
            return;
        }

        const o = await res.json();

        document.getElementById("odCustomer").textContent = o.customerName || "-";
        document.getElementById("odPhone").textContent = o.phone || "-";
        document.getElementById("odAddress").textContent = o.address || "-";

        // ✅ FIX: invoiceNo -> invoice
        document.getElementById("odInvoice").textContent = o.invoice || "-";
        document.getElementById("odDate").textContent = fmtDate(o.createdAt);

        // payment badge
        const pay = String(o.paymentMethod || "-").toUpperCase();
        const payEl = document.getElementById("odPayment");
        payEl.textContent = pay;
        payEl.className = "badge " + (pay === "CASH" ? "text-bg-info" : "text-bg-primary");

        // status badge: PAID => Completed
        const st = String(o.status || "").toUpperCase();
        const stEl = document.getElementById("odStatus");
        stEl.textContent = st === "PAID" ? "Completed" : "Pending";
        stEl.className = "badge " + (st === "PAID" ? "text-bg-success" : "text-bg-warning");

        // items
        const tbody = document.getElementById("odItems");
        tbody.innerHTML = "";

        const items = o.items || [];
        if (items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">No items</td></tr>`;
        } else {
            items.forEach((it) => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
          <td>${escapeHtml(it.product || "-")}</td>
          <td class="text-center">${Number(it.qty || 0)}</td>
          <td class="text-end">$${money2(it.price)}</td>
          <td class="text-end">$${money2(it.amount)}</td>
        `;
                tbody.appendChild(tr);
            });
        }

        document.getElementById("odSubtotal").textContent = money2(o.subtotal);
        document.getElementById("odDiscount").textContent = money2(o.discount);
        document.getElementById("odTotal").textContent = money2(o.total);

        if (orderModal) orderModal.show();
    } catch (e) {
        Swal.fire("Error", e.message || "Unknown error", "error");
    }
}

function escapeHtml(s) {
    return String(s || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
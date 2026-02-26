// src/main/resources/static/js/orders.js
let orderModal;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("orderDetailModal");
    if (modalEl && window.bootstrap) {
        orderModal = new bootstrap.Modal(modalEl);
    }

    // view
    document.querySelectorAll(".btnViewOrder").forEach((btn) => {
        btn.addEventListener("click", async () => {
            const id = btn.getAttribute("data-id");
            if (!id) return;
            await openOrderDetail(id);
        });
    });

    // status select
    document.querySelectorAll(".orderStatusSelect").forEach((sel) => {
        sel.setAttribute("data-old", sel.value);
        applyStatusColor(sel, sel.value);

        sel.addEventListener("change", async () => {
            const id = sel.getAttribute("data-id");
            if (!id) return;

            const newStatus = String(sel.value || "").toUpperCase();
            const oldStatus = String(sel.getAttribute("data-old") || "").toUpperCase();

            const ok = await Swal.fire({
                title: "Update status?",
                text: `Change to ${humanStatus(newStatus)}?`,
                icon: "question",
                showCancelButton: true,
                confirmButtonText: "Yes",
                cancelButtonText: "No",
            });

            if (!ok.isConfirmed) {
                sel.value = oldStatus;
                applyStatusColor(sel, oldStatus);
                return;
            }

            try {
                sel.disabled = true;

                const res = await fetch(`/admin/api/orders/${encodeURIComponent(id)}/status`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json", Accept: "application/json" },
                    body: JSON.stringify({ status: newStatus }),
                });

                if (!res.ok) {
                    const msg = await res.text();
                    throw new Error(msg || "Update failed");
                }

                sel.setAttribute("data-old", newStatus);
                applyStatusColor(sel, newStatus);

                Swal.fire({
                    toast: true,
                    position: "top-end",
                    icon: "success",
                    title: "Updated",
                    showConfirmButton: false,
                    timer: 900,
                });
            } catch (e) {
                sel.value = oldStatus;
                applyStatusColor(sel, oldStatus);
                Swal.fire("Error", e.message || "Cannot update status", "error");
            } finally {
                sel.disabled = false;
            }
        });
    });
});

function humanStatus(st) {
    st = String(st || "").toUpperCase();
    if (st === "PAID") return "Completed";
    if (st === "CANCELLED") return "Cancelled";
    return "Pending";
}

/* ✅ apply soft color for dropdown like sample */
function applyStatusColor(sel, st) {
    sel.classList.remove("os-paid", "os-pending", "os-cancelled");
    st = String(st || "").toUpperCase();

    if (st === "PAID") sel.classList.add("os-paid");
    else if (st === "CANCELLED") sel.classList.add("os-cancelled");
    else sel.classList.add("os-pending");
}

function money2(n) {
    return Number(n || 0).toFixed(2);
}

function fmtDate(v) {
    if (!v) return "-";
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
        document.getElementById("odPhone").textContent =
            (o.phone && String(o.phone).trim() !== "") ? o.phone : "N/A";

        document.getElementById("odAddress").textContent =
            (o.address && String(o.address).trim() !== "") ? o.address : "N/A";

        document.getElementById("odInvoice").textContent = o.invoice || "-";
        document.getElementById("odDate").textContent = fmtDate(o.createdAt);

        const pay = String(o.paymentMethod || "-").toUpperCase();
        const payEl = document.getElementById("odPayment");
        payEl.textContent = pay;
        payEl.className = "badge " + (pay === "CASH" ? "text-bg-info" : "text-bg-primary");

        const st = String(o.status || "").toUpperCase();
        const stEl = document.getElementById("odStatus");
        stEl.textContent = humanStatus(st);
        stEl.className =
            "badge " + (st === "PAID" ? "text-bg-success" : (st === "CANCELLED" ? "text-bg-danger" : "text-bg-warning"));

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
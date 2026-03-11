let orderModal;
let dt;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("orderDetailModal");
    if (modalEl && window.bootstrap) {
        orderModal = new bootstrap.Modal(modalEl);
    }

    initOrderTable();
});

function initOrderTable() {
    dt = new DataTable("#tblOrders", {
        processing: true,
        serverSide: true,
        searching: true,
        lengthMenu: [10, 25, 50, 100],
        pageLength: 10,
        ajax: {
            url: "/admin/api/orders/dt",
            type: "GET"
        },
        order: [[7, "desc"]],
        columns: [
            { data: "invoice" },
            { data: "customerName" },
            {
                data: "phone",
                render: function (data) {
                    return (data && String(data).trim() !== "") ? escapeHtml(data) : "N/A";
                }
            },
            {
                data: "totalItems",
                className: "text-center"
            },
            {
                data: "total",
                className: "text-end",
                render: function (data) {
                    return "$" + money2(data);
                }
            },
            {
                data: "paymentMethod",
                className: "text-center",
                render: function (data) {
                    const pay = String(data || "").toUpperCase();
                    if (pay === "CASH") {
                        return `<span class="pay-pill pay-cash">CASH</span>`;
                    }
                    return `<span class="pay-pill pay-khqr">KHQR</span>`;
                }
            },
            {
                data: "status",
                className: "text-center",
                orderable: false,
                render: function (data, type, row) {
                    const st = String(data || "").toUpperCase();

                    return `
                        <select class="form-select form-select-sm order-status-select ${statusClass(st)}"
                                data-id="${row.id}"
                                data-old="${st}">
                            <option value="PAID" ${st === "PAID" ? "selected" : ""}>Completed</option>
                            <option value="PENDING" ${st === "PENDING" ? "selected" : ""}>Pending</option>
                            <option value="CANCELLED" ${st === "CANCELLED" ? "selected" : ""}>Cancelled</option>
                        </select>
                    `;
                }
            },
            {
                data: "createdAt",
                render: function (data) {
                    return fmtDate(data);
                }
            },
            {
                data: null,
                className: "text-center",
                orderable: false,
                render: function (row) {
                    return `
                        <button type="button"
                                class="btn btn-info btn-sm btn-view btnViewOrder"
                                data-id="${row.id}">
                            <i class="bi bi-eye"></i> View
                        </button>
                    `;
                }
            }
        ],
        drawCallback: function () {
            bindViewButtons();
            bindStatusSelects();
        }
    });
}

function bindViewButtons() {
    document.querySelectorAll(".btnViewOrder").forEach((btn) => {
        btn.onclick = async () => {
            const id = btn.getAttribute("data-id");
            if (!id) return;
            await openOrderDetail(id);
        };
    });
}

function bindStatusSelects() {
    document.querySelectorAll(".order-status-select").forEach((sel) => {
        sel.onchange = async () => {
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
                cancelButtonText: "No"
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
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify({ status: newStatus })
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
                    timer: 1000
                });
            } catch (e) {
                sel.value = oldStatus;
                applyStatusColor(sel, oldStatus);
                Swal.fire("Error", e.message || "Cannot update status", "error");
            } finally {
                sel.disabled = false;
            }
        };

        applyStatusColor(sel, sel.value);
    });
}

function statusClass(st) {
    st = String(st || "").toUpperCase();
    if (st === "PAID") return "os-paid";
    if (st === "CANCELLED") return "os-cancelled";
    return "os-pending";
}

function applyStatusColor(sel, st) {
    sel.classList.remove("os-paid", "os-pending", "os-cancelled");
    sel.classList.add(statusClass(st));
}

function humanStatus(st) {
    st = String(st || "").toUpperCase();
    if (st === "PAID") return "Completed";
    if (st === "CANCELLED") return "Cancelled";
    return "Pending";
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
            headers: { Accept: "application/json" }
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
            "badge " + (st === "PAID"
                ? "text-bg-success"
                : (st === "CANCELLED" ? "text-bg-danger" : "text-bg-warning"));

        const tbody = document.getElementById("odItems");
        tbody.innerHTML = "";

        const items = o.items || [];
        if (items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">No items</td></tr>`;
        } else {
            items.forEach((it) => {
                const originalPriceHtml =
                    Number(it.originalPrice || 0) > Number(it.price || 0)
                        ? `<div class="small text-muted text-decoration-line-through">$${money2(it.originalPrice)}</div>`
                        : "";

                const discountHtml =
                    Number(it.discountAmount || 0) > 0
                        ? `<div class="small text-danger">Save $${money2(it.discountAmount)}</div>`
                        : "";

                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>
                        <div class="fw-semibold">${escapeHtml(it.product || "-")}</div>
                        <div class="small text-muted">${escapeHtml(it.size || "N/A")}</div>
                    </td>
                    <td class="text-center">${Number(it.qty || 0)}</td>
                    <td class="text-end">
                        ${originalPriceHtml}
                        <div>$${money2(it.price)}</div>
                        ${discountHtml}
                    </td>
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
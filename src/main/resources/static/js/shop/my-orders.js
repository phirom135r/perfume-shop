let orderModal;
let currentOrderId = null;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("orderDetailModal");
    orderModal = new bootstrap.Modal(modalEl);

    document.querySelectorAll(".btn-view-order").forEach(btn => {
        btn.addEventListener("click", async function () {
            const id = this.dataset.id;
            await openOrderDetail(id);
        });
    });

    document.getElementById("btnDownloadInvoice").addEventListener("click", function () {
        if (!currentOrderId) return;

        Swal.fire({
            icon: "info",
            title: "Coming Soon",
            text: "Invoice PDF download will be added next."
        });
    });
});

function money(v) {
    return "$" + Number(v || 0).toFixed(2);
}

function fmtDate(v) {
    if (!v) return "-";
    const d = new Date(v);
    return d.toLocaleString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function escapeHtml(s) {
    return String(s || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function applyStatusPill(el, status) {
    const st = String(status || "").toUpperCase();
    el.className = "status-pill";

    if (st === "PAID") {
        el.classList.add("status-paid");
        el.textContent = "PAID";
    } else if (st === "CANCELLED") {
        el.classList.add("status-cancelled");
        el.textContent = "CANCELLED";
    } else {
        el.classList.add("status-pending");
        el.textContent = st || "PENDING";
    }
}

function applyPaymentPill(el, payment) {
    const pay = String(payment || "").toUpperCase();
    el.className = "payment-pill";

    if (pay === "KHQR") {
        el.classList.add("payment-khqr");
        el.textContent = "KHQR";
    } else {
        el.classList.add("payment-cash");
        el.textContent = pay || "CASH";
    }
}

async function openOrderDetail(id) {
    try {
        const res = await fetch(`/perfume-shop/api/my-orders/${id}`, {
            headers: { "Accept": "application/json" }
        });

        if (!res.ok) {
            await Swal.fire({
                icon: "error",
                title: "Cannot load order detail"
            });
            return;
        }

        const o = await res.json();
        currentOrderId = o.id;

        document.getElementById("odInvoice").textContent = o.invoice || "-";
        document.getElementById("odCustomer").textContent = o.customerName || "-";
        document.getElementById("odPhone").textContent = o.phone || "-";
        document.getElementById("odAddress").textContent = o.address || "-";
        document.getElementById("odDate").textContent = fmtDate(o.createdAt);
        document.getElementById("odTotal").textContent = money(o.total);
        document.getElementById("odSubtotal").textContent = money(o.subtotal);
        document.getElementById("odDiscount").textContent = "-" + money(o.discount);
        document.getElementById("odGrandTotal").textContent = money(o.total);

        applyStatusPill(document.getElementById("odStatus"), o.status);
        applyPaymentPill(document.getElementById("odPayment"), o.paymentMethod);

        const tbody = document.getElementById("odItems");
        tbody.innerHTML = "";

        (o.items || []).forEach(it => {
            const tr = document.createElement("tr");
            const img = it.image ? it.image : "/images/no-image.png";

            const originalPriceHtml =
                Number(it.originalPrice || 0) > Number(it.unitPrice || 0)
                    ? `<div class="small text-muted text-decoration-line-through">${money(it.originalPrice)}</div>`
                    : "";

            const discountHtml =
                Number(it.discountAmount || 0) > 0
                    ? `<div class="small text-danger">Save ${money(it.discountAmount)}</div>`
                    : "";

            tr.innerHTML = `
                <td>
                    <div class="d-flex align-items-center gap-3">
                        <img src="${img}" class="thumb" alt="product">
                        <div>
                            <div class="product-name">${escapeHtml(it.productName)}</div>
                            <div class="product-sub">${escapeHtml(it.size || "N/A")}</div>
                        </div>
                    </div>
                </td>
                <td class="text-center">${it.qty ?? 0}</td>
                <td class="text-end">
                    ${originalPriceHtml}
                    <div>${money(it.unitPrice)}</div>
                    ${discountHtml}
                </td>
                <td class="text-end">${money(it.lineTotal)}</td>
            `;
            tbody.appendChild(tr);
        });

        if ((o.items || []).length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center text-muted py-4">No items</td>
                </tr>
            `;
        }

        orderModal.show();
    } catch (e) {
        await Swal.fire({
            icon: "error",
            title: "Cannot load order detail"
        });
    }
}
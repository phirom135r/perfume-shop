// ===== STATE =====
let page = 0;
const size = 9;
let currentQuery = "";

// cart: Map(productId -> {id,name,price,imageUrl,qty})
const cart = new Map();

// ===== KHQR MODAL =====
let khqrModal;
let verifyTimer = null;
let currentMd5 = null;

// ===== COUNTDOWN =====
const KHQR_TTL_SECONDS = 180; // 3:00
let countdownTimer = null;
let remainingSeconds = KHQR_TTL_SECONDS;

document.addEventListener("DOMContentLoaded", () => {
    // modal
    const modalEl = document.getElementById("khqrModal");
    if (modalEl && window.bootstrap) {
        khqrModal = new bootstrap.Modal(modalEl, { backdrop: "static", keyboard: false });

        modalEl.addEventListener("hidden.bs.modal", () => {
            stopKhqrPolling();
            stopCountdown();
            currentMd5 = null;
        });
    }

    // events
    document.getElementById("btnSearch")?.addEventListener("click", () => {
        page = 0;
        currentQuery = (document.getElementById("q").value || "").trim();
        loadProducts();
    });

    document.getElementById("prevBtn")?.addEventListener("click", () => {
        if (page > 0) {
            page--;
            loadProducts();
        }
    });

    document.getElementById("nextBtn")?.addEventListener("click", () => {
        page++;
        loadProducts();
    });

    document.getElementById("btnClear")?.addEventListener("click", () => {
        cart.clear();
        renderCart();
    });

    document.getElementById("btnComplete")?.addEventListener("click", completeOrder);

    // discount input -> recalc totals live
    document.getElementById("extraDiscount")?.addEventListener("input", updateTotals);

    // cancel KHQR (X + button)
    document.querySelectorAll(".btnCancelKhqr").forEach(btn => {
        btn.addEventListener("click", () => {
            stopKhqrPolling();
            stopCountdown();
            currentMd5 = null;
            if (khqrModal) khqrModal.hide();
        });
    });

    // initial load
    loadProducts();
    renderCart();
});

// ===== HELPERS =====
function money2(n) {
    const num = Number(n || 0);
    return num.toFixed(2);
}

function parseMoneyInput(v) {
    const n = Number(String(v || "0").replace(/[^\d.]/g, ""));
    return Number.isFinite(n) ? n : 0;
}

function escapeHtml(s) {
    return String(s || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

// ===== SweetAlert Toast =====
function toastSuccess(title) {
    if (!window.Swal) return;
    Swal.fire({
        toast: true,
        position: "top-end",
        icon: "success",
        title,
        showConfirmButton: false,
        timer: 900,
        timerProgressBar: true
    });
}

// ======================================================
// COUNTDOWN
// ======================================================
function formatMMSS(sec) {
    const mm = String(Math.floor(sec / 60)).padStart(2, "0");
    const ss = String(sec % 60).padStart(2, "0");
    return `${mm}:${ss}`;
}

function renderTimeLeft() {
    const el = document.getElementById("khqrTimeLeft");
    if (el) el.textContent = formatMMSS(remainingSeconds);
}

function startCountdown(seconds = KHQR_TTL_SECONDS) {
    stopCountdown();

    remainingSeconds = seconds;
    renderTimeLeft();

    countdownTimer = setInterval(() => {
        remainingSeconds--;
        renderTimeLeft();

        if (remainingSeconds <= 0) {
            stopCountdown();
            stopKhqrPolling();

            const hint = document.getElementById("khqrHint");
            if (hint) {
                hint.textContent = "QR expired. Please try again.";
                hint.className = "text-danger mt-2 text-center";
            }

            setTimeout(() => {
                currentMd5 = null;
                if (khqrModal) khqrModal.hide();
            }, 800);
        }
    }, 1000);
}

function stopCountdown() {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
}

// ======================================================
// PRODUCTS
// ======================================================
async function loadProducts() {
    const grid = document.getElementById("productsGrid");
    if (!grid) return;

    grid.innerHTML = `<div class="text-muted">Loading...</div>`;

    try {
        const url = `/admin/api/products/pos?q=${encodeURIComponent(currentQuery)}&page=${page}&size=${size}`;
        const res = await fetch(url, { headers: { "Accept": "application/json" } });

        if (!res.ok) {
            grid.innerHTML = `<div class="text-danger">Failed to load products (HTTP ${res.status})</div>`;
            return;
        }

        const data = await res.json();
        const content = data.content || [];
        const totalPages = Number(data.totalPages || 1);

        document.getElementById("pageInfo").textContent =
            `Page ${Number(data.number || 0) + 1} / ${totalPages}`;

        document.getElementById("prevBtn").disabled = (page <= 0);
        document.getElementById("nextBtn").disabled = (page + 1 >= totalPages);

        if (content.length === 0) {
            grid.innerHTML = `<div class="text-muted">No products found.</div>`;
            return;
        }

        grid.innerHTML = "";

        content.forEach(p => {
            const col = document.createElement("div");
            col.className = "col-12 col-sm-6 col-md-4";

            const imgSrc = p.imageUrl || "/images/no-image.png";

            col.innerHTML = `
        <div class="card card-soft product-card h-100">
          <img class="card-img-top" src="${imgSrc}" alt="">
          <div class="card-body">
            <div class="fw-semibold">${escapeHtml(p.name || "")}</div>
            <div class="small text-muted">${escapeHtml(p.brand || "")}</div>
            <div class="mt-2 d-flex justify-content-between align-items-center">
              <div class="fw-bold text-danger">$${money2(p.price)}</div>
              <span class="badge bg-success-subtle text-success">${(p.stock ?? 0)} in</span>
            </div>
            <button class="btn btn-primary w-100 mt-3" type="button" data-add="${p.id}">
              Add to Cart
            </button>
          </div>
        </div>
      `;

            grid.appendChild(col);
        });

        // bind add buttons
        grid.querySelectorAll("[data-add]").forEach(btn => {
            btn.addEventListener("click", () => {
                const id = Number(btn.getAttribute("data-add"));
                const p = content.find(x => Number(x.id) === id);
                if (!p) return;
                addToCart(p);
            });
        });

    } catch (e) {
        grid.innerHTML = `<div class="text-danger">Error: ${escapeHtml(e.message || "unknown")}</div>`;
    }
}

// ======================================================
// CART
// ======================================================
function addToCart(p) {
    const id = Number(p.id);
    if (!id) return;

    const existing = cart.get(id);
    if (existing) {
        existing.qty += 1;
    } else {
        cart.set(id, {
            id,
            name: p.name || "",
            price: Number(p.price || 0),
            imageUrl: p.imageUrl || "",
            qty: 1
        });
    }

    // ✅ toast message
    toastSuccess("Added to cart");

    renderCart();
}

function renderCart() {
    const list = document.getElementById("cartList");
    if (!list) return;

    list.innerHTML = "";

    if (cart.size === 0) {
        list.innerHTML = `<div class="text-muted">Cart is empty</div>`;
        updateTotals();
        return;
    }

    for (const item of cart.values()) {
        const row = document.createElement("div");
        row.className = "cart-item";

        row.innerHTML = `
      <div class="d-flex gap-2 align-items-center">
        <div class="flex-grow-1">
          <div class="cart-title">${escapeHtml(item.name)}</div>
          <div class="cart-sub">$${money2(item.price)} each</div>
        </div>

        <div class="d-flex align-items-center gap-1">
          <button class="btn btn-outline-secondary qty-btn" type="button" data-dec="${item.id}">-</button>
          <div style="min-width:28px;text-align:center;">${item.qty}</div>
          <button class="btn btn-outline-secondary qty-btn" type="button" data-inc="${item.id}">+</button>
          <button class="btn btn-outline-danger qty-btn" type="button" data-del="${item.id}">×</button>
        </div>
      </div>
    `;

        list.appendChild(row);
    }

    // bind qty buttons
    list.querySelectorAll("[data-inc]").forEach(b => {
        b.addEventListener("click", () => {
            const id = Number(b.getAttribute("data-inc"));
            const it = cart.get(id);
            if (!it) return;
            it.qty += 1;
            renderCart();
        });
    });

    list.querySelectorAll("[data-dec]").forEach(b => {
        b.addEventListener("click", () => {
            const id = Number(b.getAttribute("data-dec"));
            const it = cart.get(id);
            if (!it) return;
            it.qty = Math.max(1, it.qty - 1);
            renderCart();
        });
    });

    list.querySelectorAll("[data-del]").forEach(b => {
        b.addEventListener("click", () => {
            const id = Number(b.getAttribute("data-del"));
            cart.delete(id);
            renderCart();
        });
    });

    updateTotals();
}

function updateTotals() {
    let subtotal = 0;
    for (const it of cart.values()) {
        subtotal += Number(it.price || 0) * Number(it.qty || 0);
    }

    const discountInput = document.getElementById("extraDiscount");
    const discount = parseMoneyInput(discountInput?.value);
    const discountClamped = Math.max(0, Math.min(discount, subtotal));
    const total = Math.max(0, subtotal - discountClamped);

    document.getElementById("subtotal").textContent = money2(subtotal);
    document.getElementById("discountShow").textContent = money2(discountClamped);
    document.getElementById("total").textContent = money2(total);
}

// ======================================================
// CHECKOUT
// ======================================================
function getCartItems() {
    const items = [];
    for (const it of cart.values()) {
        items.push({ productId: it.id, qty: it.qty });
    }
    return items;
}

async function completeOrder() {
    const items = getCartItems();
    const customerName = (document.getElementById("customerName")?.value || "").trim();

    if (!items.length) {
        Swal.fire("Cart empty", "Please add product", "warning");
        return;
    }
    if (!customerName) {
        Swal.fire("Customer required", "Please input customer name", "warning");
        return;
    }

    const payload = {
        customerName,
        phone: (document.getElementById("phone")?.value || "").trim(),
        address: (document.getElementById("address")?.value || "").trim(),
        discount: Number(parseMoneyInput(document.getElementById("extraDiscount")?.value) || 0),
        paymentMethod: (document.getElementById("paymentMethod")?.value || "CASH").toUpperCase(),
        items
    };

    try {
        const res = await fetch("/admin/pos/checkout", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const msg = await res.text();
            Swal.fire("Checkout failed", msg, "error");
            return;
        }

        const data = await res.json();

        // CASH flow
        if (data.redirectUrl) {
            window.location.href = data.redirectUrl;
            return;
        }

        // KHQR flow must return: invoice, amount, md5, khqrString
        if (data.md5 && data.khqrString) {
            openKhqrModal(data);
            return;
        }

        Swal.fire("Error", "Invalid response from server", "error");
    } catch (e) {
        Swal.fire("Error", e.message || "Network error", "error");
    }
}

// ======================================================
// KHQR MODAL
// ======================================================
function openKhqrModal(data) {
    document.getElementById("khqrInvoice").textContent = data.invoice || "-";
    document.getElementById("khqrAmount").textContent = money2(data.amount);

    const hint = document.getElementById("khqrHint");
    if (hint) {
        hint.textContent = "Waiting for payment...";
        hint.className = "text-muted mt-2 text-center";
    }

    const box = document.getElementById("khqrQrBox");
    box.innerHTML = "";
    new QRCode(box, {
        text: data.khqrString,
        width: 220,
        height: 220
    });

    currentMd5 = data.md5;

    if (khqrModal) khqrModal.show();

    startCountdown();
    startKhqrPolling();
}

// ======================================================
// VERIFY POLLING
// ======================================================
function startKhqrPolling() {
    stopKhqrPolling();
    if (!currentMd5) return;

    verifyTimer = setInterval(async () => {
        try {
            const res = await fetch(`/admin/api/payments/verify?md5=${encodeURIComponent(currentMd5)}`, {
                method: "GET",
                headers: { "Accept": "application/json" }
            });

            if (!res.ok) return;

            const json = await res.json();
            const st = String(json.status || "").toUpperCase();

            const hint = document.getElementById("khqrHint");

            if (st === "PAID") {
                stopKhqrPolling();
                stopCountdown();

                if (khqrModal) khqrModal.hide();

                Swal.fire({
                    title: "Thanks for ordering 🎉",
                    text: "Payment successfully",
                    icon: "success",
                    confirmButtonText: "OK",
                    allowOutsideClick: false,
                    allowEscapeKey: false
                }).then((result) => {
                    if (result.isConfirmed) {
                        // ✅ clear cart after success
                        cart.clear();
                        renderCart();
                        window.location.href = "/admin/orders";
                    }
                });

                return;
            }

            if (st === "NOT_FOUND") {
                if (hint) {
                    hint.textContent = "Order not found (md5).";
                    hint.className = "text-danger mt-2 text-center";
                }
            } else {
                if (hint) {
                    hint.textContent = "Waiting for payment...";
                    hint.className = "text-muted mt-2 text-center";
                }
            }
        } catch (e) {
            // ignore
        }
    }, 3000);
}

function stopKhqrPolling() {
    if (verifyTimer) {
        clearInterval(verifyTimer);
        verifyTimer = null;
    }
}
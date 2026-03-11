let page = 0;
const size = 9;

let currentQuery = "";
let currentCategoryId = "";
let currentBrandId = "";

const cart = new Map();

let khqrModal;
let verifyTimer = null;
let currentMd5 = null;

const KHQR_TTL_SECONDS = 180;
let countdownTimer = null;
let remainingSeconds = KHQR_TTL_SECONDS;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("khqrModal");
    if (modalEl && window.bootstrap) {
        khqrModal = new bootstrap.Modal(modalEl, {
            backdrop: "static",
            keyboard: false
        });

        modalEl.addEventListener("hidden.bs.modal", () => {
            stopKhqrPolling();
            stopCountdown();
            currentMd5 = null;
        });
    }

    document.getElementById("btnSearch")?.addEventListener("click", () => {
        page = 0;
        currentQuery = (document.getElementById("q")?.value || "").trim();
        currentCategoryId = document.getElementById("categoryFilter")?.value || "";
        currentBrandId = document.getElementById("brandFilter")?.value || "";
        loadProducts();
    });

    document.getElementById("btnReset")?.addEventListener("click", () => {
        page = 0;
        currentQuery = "";
        currentCategoryId = "";
        currentBrandId = "";

        const q = document.getElementById("q");
        const category = document.getElementById("categoryFilter");
        const brand = document.getElementById("brandFilter");

        if (q) q.value = "";
        if (category) category.value = "";
        if (brand) brand.value = "";

        loadProducts();
    });

    document.getElementById("q")?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            page = 0;
            currentQuery = (document.getElementById("q")?.value || "").trim();
            currentCategoryId = document.getElementById("categoryFilter")?.value || "";
            currentBrandId = document.getElementById("brandFilter")?.value || "";
            loadProducts();
        }
    });

    document.getElementById("categoryFilter")?.addEventListener("change", () => {
        page = 0;
        currentCategoryId = document.getElementById("categoryFilter")?.value || "";
        loadProducts();
    });

    document.getElementById("brandFilter")?.addEventListener("change", () => {
        page = 0;
        currentBrandId = document.getElementById("brandFilter")?.value || "";
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
        loadProducts();
    });

    document.getElementById("btnComplete")?.addEventListener("click", completeOrder);
    document.getElementById("extraDiscount")?.addEventListener("input", updateTotals);

    document.querySelectorAll(".btnCancelKhqr").forEach((btn) => {
        btn.addEventListener("click", async () => {
            await cancelPayment();
            stopKhqrPolling();
            stopCountdown();
            currentMd5 = null;
            if (khqrModal) khqrModal.hide();
        });
    });

    loadCategories();
    loadBrands();
    loadProducts();
    renderCart();
});

function money2(n) {
    return Number(n || 0).toFixed(2);
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

function notEnoughStock(productName) {
    if (!window.Swal) return;
    Swal.fire("Not enough stock", `Not enough stock for product: ${productName}`, "warning");
}

async function loadCategories() {
    const select = document.getElementById("categoryFilter");
    if (!select) return;

    try {
        const res = await fetch("/admin/api/categories/simple", {
            headers: { Accept: "application/json" }
        });

        if (!res.ok) return;

        const cats = await res.json();

        select.innerHTML = `<option value="">All Categories</option>`;
        cats.forEach(c => {
            const option = document.createElement("option");
            option.value = c.id;
            option.textContent = c.name;
            select.appendChild(option);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadBrands() {
    const select = document.getElementById("brandFilter");
    if (!select) return;

    try {
        const res = await fetch("/admin/api/brands/active", {
            headers: { Accept: "application/json" }
        });

        if (!res.ok) return;

        const brands = await res.json();

        select.innerHTML = `<option value="">All Brands</option>`;
        brands.forEach(b => {
            const option = document.createElement("option");
            option.value = b.id;
            option.textContent = b.name;
            select.appendChild(option);
        });
    } catch (e) {
        console.error(e);
    }
}

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

    countdownTimer = setInterval(async () => {
        remainingSeconds--;
        renderTimeLeft();

        if (remainingSeconds <= 0) {
            stopCountdown();
            stopKhqrPolling();
            await cancelPayment();

            const hint = document.getElementById("khqrHint");
            if (hint) {
                hint.textContent = "QR expired. Payment cancelled.";
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

async function loadProducts() {
    const grid = document.getElementById("productsGrid");
    if (!grid) return;

    grid.innerHTML = `<div class="text-muted">Loading...</div>`;

    try {
        const url =
            `/admin/api/products/pos?q=${encodeURIComponent(currentQuery)}` +
            `&categoryId=${encodeURIComponent(currentCategoryId || "")}` +
            `&brandId=${encodeURIComponent(currentBrandId || "")}` +
            `&page=${page}&size=${size}`;

        const res = await fetch(url, { headers: { Accept: "application/json" } });

        if (!res.ok) {
            grid.innerHTML = `<div class="text-danger">Failed to load products (HTTP ${res.status})</div>`;
            return;
        }

        const data = await res.json();
        const content = data.content || [];
        const totalPages = Number(data.totalPages || 1);

        const pageInfo = document.getElementById("pageInfo");
        if (pageInfo) pageInfo.textContent = `Page ${Number(data.number || 0) + 1} / ${totalPages}`;

        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");
        if (prevBtn) prevBtn.disabled = page <= 0;
        if (nextBtn) nextBtn.disabled = page + 1 >= totalPages;

        if (content.length === 0) {
            grid.innerHTML = `<div class="text-muted">No products found.</div>`;
            return;
        }

        grid.innerHTML = "";

        content.forEach((p) => {
            const col = document.createElement("div");
            col.className = "col-12 col-sm-6 col-lg-4";

            const imgSrc = p.imageUrl || "/images/no-image.png";
            const stock = Number(p.stock ?? 0);

            const inCart = cart.get(Number(p.id));
            const reached = inCart ? Number(inCart.qty || 0) >= stock : false;
            const disableAdd = stock <= 0 || reached;

            const name = escapeHtml(p.name || "");
            const brand = escapeHtml(p.brand || "");
            const brandHtml = brand ? `<div class="p-brand">${brand}</div>` : `<div class="p-brand"></div>`;

            const originalPrice = Number(p.price ?? 0);
            const discountAmount = Number(p.discount ?? 0);
            const finalPrice = Number(p.finalPrice ?? (originalPrice - discountAmount));
            const showDiscount = discountAmount > 0;
            const percent = originalPrice > 0 ? Math.round((discountAmount / originalPrice) * 100) : 0;
            const pctHtml = (showDiscount && percent > 0)
                ? `<span class="badge bg-danger ms-2">-${percent}%</span>`
                : "";

            const priceHtml = showDiscount
                ? `
                    <div class="p-price-wrap">
                        <div class="p-price">$${money2(finalPrice)} ${pctHtml}</div>
                        <div class="p-old-price">$${money2(originalPrice)}</div>
                    </div>
                  `
                : `
                    <div class="p-price-wrap">
                        <div class="p-price">$${money2(originalPrice)}</div>
                    </div>
                  `;

            let badgeClass = "badge-high";
            let badgeText = "High Stock";
            if (stock <= 0) {
                badgeClass = "badge-out";
                badgeText = "Out of Stock";
            } else if (stock <= 5) {
                badgeClass = "badge-low";
                badgeText = "Low Stock";
            }

            const badgeHtml = `<div class="p-badge ${badgeClass}">${badgeText}</div>`;

            col.innerHTML = `
                <div class="p-card h-100">
                    ${badgeHtml}
                    <img class="p-img" src="${imgSrc}" alt="">
                    <div class="p-body">
                        <div class="p-name">${name}</div>
                        ${brandHtml}

                        <div class="p-meta">
                            ${priceHtml}
                            <div class="p-stock">${stock} in</div>
                        </div>

                        <div class="p-actions">
                            <button class="btn btn-primary btn-add"
                                    type="button"
                                    data-add="${p.id}"
                                    ${disableAdd ? "disabled" : ""}>
                                ${stock <= 0 ? "Out of Stock" : reached ? "Max in Cart" : "Add to Cart"}
                            </button>
                        </div>
                    </div>
                </div>
            `;

            grid.appendChild(col);
        });

        grid.querySelectorAll("[data-add]").forEach((btn) => {
            btn.addEventListener("click", () => {
                const id = Number(btn.getAttribute("data-add"));
                const p = content.find((x) => Number(x.id) === id);
                if (!p) return;
                addToCart(p);
                loadProducts();
            });
        });

    } catch (e) {
        grid.innerHTML = `<div class="text-danger">Error: ${escapeHtml(e.message || "unknown")}</div>`;
    }
}

function addToCart(p) {
    const id = Number(p.id);
    if (!id) return;

    const stock = Number(p.stock ?? 0);
    if (stock <= 0) {
        notEnoughStock(p.name || "");
        return;
    }

    const existing = cart.get(id);

    if (existing) {
        if (Number(existing.qty || 0) >= Number(existing.stock ?? 0)) {
            notEnoughStock(existing.name);
            return;
        }
        existing.qty += 1;
    } else {
        const originalPrice = Number(p.price ?? 0);
        const discountAmount = Number(p.discount ?? 0);
        const finalPrice = Number(p.finalPrice ?? (originalPrice - discountAmount));

        cart.set(id, {
            id,
            name: p.name || "",
            originalPrice,
            unitPrice: finalPrice,
            discountAmount,
            imageUrl: p.imageUrl || "",
            qty: 1,
            stock: stock
        });
    }

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

        const stock = Number(item.stock ?? 0);
        const atMax = Number(item.qty || 0) >= stock && stock > 0;

        const oldPriceHtml = Number(item.originalPrice || 0) > Number(item.unitPrice || 0)
            ? `<div class="cart-old">$${money2(item.originalPrice)}</div>`
            : "";

        row.innerHTML = `
            <div class="d-flex gap-2 align-items-center">
                <div class="flex-grow-1">
                    <div class="cart-title">${escapeHtml(item.name)}</div>
                    ${oldPriceHtml}
                    <div class="cart-sub">$${money2(item.unitPrice)} each ${stock ? `• Stock: ${stock}` : ""}</div>
                </div>

                <div class="d-flex align-items-center gap-1">
                    <button class="btn btn-outline-secondary qty-btn" type="button" data-dec="${item.id}">-</button>
                    <div style="min-width:28px;text-align:center;">${item.qty}</div>
                    <button class="btn btn-outline-secondary qty-btn" type="button" data-inc="${item.id}" ${atMax ? "disabled" : ""}>+</button>
                    <button class="btn btn-outline-danger qty-btn" type="button" data-del="${item.id}">×</button>
                </div>
            </div>
        `;

        list.appendChild(row);
    }

    list.querySelectorAll("[data-inc]").forEach((b) => {
        b.addEventListener("click", () => {
            const id = Number(b.getAttribute("data-inc"));
            const it = cart.get(id);
            if (!it) return;

            const stock = Number(it.stock ?? 0);
            if (stock <= 0 || it.qty >= stock) {
                notEnoughStock(it.name);
                return;
            }

            it.qty += 1;
            renderCart();
            loadProducts();
        });
    });

    list.querySelectorAll("[data-dec]").forEach((b) => {
        b.addEventListener("click", () => {
            const id = Number(b.getAttribute("data-dec"));
            const it = cart.get(id);
            if (!it) return;

            it.qty = Math.max(1, it.qty - 1);
            renderCart();
            loadProducts();
        });
    });

    list.querySelectorAll("[data-del]").forEach((b) => {
        b.addEventListener("click", () => {
            const id = Number(b.getAttribute("data-del"));
            cart.delete(id);
            renderCart();
            loadProducts();
        });
    });

    updateTotals();
}

function updateTotals() {
    let subtotal = 0;
    let productDiscount = 0;

    for (const it of cart.values()) {
        const qty = Number(it.qty || 0);
        subtotal += Number(it.originalPrice || 0) * qty;
        productDiscount += Number(it.discountAmount || 0) * qty;
    }

    const discountInput = document.getElementById("extraDiscount");
    const extraDiscount = parseMoneyInput(discountInput?.value);

    let extraDiscountClamped = Math.max(0, extraDiscount);
    const maxExtra = Math.max(0, subtotal - productDiscount);
    if (extraDiscountClamped > maxExtra) {
        extraDiscountClamped = maxExtra;
    }

    const total = Math.max(0, subtotal - productDiscount - extraDiscountClamped);

    document.getElementById("subtotal").textContent = money2(subtotal);
    document.getElementById("productDiscountShow").textContent = money2(productDiscount);
    document.getElementById("discountShow").textContent = money2(extraDiscountClamped);
    document.getElementById("total").textContent = money2(total);
}

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
            body: JSON.stringify(payload),
        });

        if (!res.ok) {
            const msg = await res.text();
            Swal.fire("Checkout failed", msg, "error");
            return;
        }

        const data = await res.json();

        if (data.redirectUrl) {
            window.location.href = data.redirectUrl;
            return;
        }
        if (data.md5 && data.khqrString) {
            openKhqrModal(data);
            return;
        }

        Swal.fire("Error", "Invalid response from server", "error");
    } catch (e) {
        Swal.fire("Error", e.message || "Network error", "error");
    }
}

function openKhqrModal(data) {
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

async function cancelPayment() {
    if (!currentMd5) return;
    try {
        await fetch(`/admin/api/payments/cancel?md5=${encodeURIComponent(currentMd5)}`, {
            method: "POST",
            headers: { Accept: "application/json" },
        });
    } catch (e) {
        console.error(e);
    }
}

function startKhqrPolling() {
    stopKhqrPolling();
    if (!currentMd5) return;

    verifyTimer = setInterval(async () => {
        try {
            const res = await fetch(`/admin/api/payments/verify?md5=${encodeURIComponent(currentMd5)}`, {
                method: "GET",
                headers: { Accept: "application/json" },
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
                    allowEscapeKey: false,
                }).then((result) => {
                    if (result.isConfirmed) {
                        cart.clear();
                        renderCart();
                        window.location.href = "/admin/orders";
                    }
                });
                return;
            }

            if (st === "CANCELLED") {
                stopKhqrPolling();
                stopCountdown();

                if (hint) {
                    hint.textContent = "Payment cancelled.";
                    hint.className = "text-danger mt-2 text-center";
                }

                setTimeout(() => {
                    currentMd5 = null;
                    if (khqrModal) khqrModal.hide();
                }, 700);
                return;
            }

            if (st === "OUT_OF_STOCK") {
                stopKhqrPolling();
                stopCountdown();
                if (khqrModal) khqrModal.hide();
                Swal.fire("Out of stock", "Stock not enough for this order.", "error");
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
            console.error(e);
        }
    }, 3000);
}

function stopKhqrPolling() {
    if (verifyTimer) {
        clearInterval(verifyTimer);
        verifyTimer = null;
    }
}
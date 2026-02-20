// ===============================
// POS SYSTEM JS
// ===============================

let page = 0;
const size = 9;
let q = "";
const cart = new Map();

// -----------------------------
// Helpers
// -----------------------------
function money(n) {
    return Number(n || 0).toFixed(2);
}

function cartSubtotal() {
    let s = 0;
    cart.forEach(it => {
        s += Number(it.price) * it.qty;
    });
    return s;
}

function discountValue() {
    const v = document.getElementById("extraDiscount").value || 0;
    return Number(v);
}

function updateSummary() {
    const sub = cartSubtotal();
    const discount = Math.min(discountValue(), sub);
    const total = sub - discount;

    document.getElementById("subtotal").textContent = money(sub);
    document.getElementById("discountShow").textContent = money(discount);
    document.getElementById("total").textContent = money(total);
}

// -----------------------------
// Render Cart
// -----------------------------
function renderCart() {
    const wrap = document.getElementById("cartList");
    wrap.innerHTML = "";

    if (cart.size === 0) {
        wrap.innerHTML = `<div class="text-muted small">Cart is empty</div>`;
        updateSummary();
        return;
    }

    cart.forEach(it => {
        const row = document.createElement("div");
        row.className = "border rounded p-2 d-flex align-items-center gap-2";

        row.innerHTML = `
            <div class="flex-grow-1">
                <div class="fw-bold">${it.name}</div>
                <div class="small">$${money(it.price)}</div>
            </div>
            <div class="d-flex align-items-center gap-2">
                <button class="btn btn-sm btn-outline-secondary" data-dec="${it.id}">-</button>
                <span>${it.qty}</span>
                <button class="btn btn-sm btn-outline-secondary" data-inc="${it.id}">+</button>
                <button class="btn btn-sm btn-outline-danger" data-remove="${it.id}">x</button>
            </div>
        `;
        wrap.appendChild(row);
    });

    wrap.querySelectorAll("[data-inc]").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = Number(btn.dataset.inc);
            const item = cart.get(id);
            item.qty++;
            renderCart();
        });
    });

    wrap.querySelectorAll("[data-dec]").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = Number(btn.dataset.dec);
            const item = cart.get(id);
            item.qty = Math.max(1, item.qty - 1);
            renderCart();
        });
    });

    wrap.querySelectorAll("[data-remove]").forEach(btn => {
        btn.addEventListener("click", () => {
            cart.delete(Number(btn.dataset.remove));
            renderCart();
        });
    });

    updateSummary();
}

// -----------------------------
// Load Products
// -----------------------------
async function loadProducts() {
    const url = `/admin/api/products/pos?q=${encodeURIComponent(q)}&page=${page}&size=${size}`;

    const res = await fetch(url);
    const data = await res.json();

    const grid = document.getElementById("productsGrid");
    grid.innerHTML = "";

    data.content.forEach(p => {
        const col = document.createElement("div");
        col.className = "col-12 col-md-6 col-lg-4";

        col.innerHTML = `
            <div class="card shadow-sm">
                <img src="${p.image}" class="card-img-top" style="height:160px;object-fit:cover">
                <div class="card-body">
                    <h6>${p.name}</h6>
                    <div class="text-danger fw-bold">$${money(p.price)}</div>
                    <button class="btn btn-primary w-100 mt-2" data-add="${p.id}">
                        Add to Cart
                    </button>
                </div>
            </div>
        `;
        grid.appendChild(col);
    });

    grid.querySelectorAll("[data-add]").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = Number(btn.dataset.add);
            const p = data.content.find(x => x.id === id);

            if (cart.has(id)) {
                cart.get(id).qty++;
            } else {
                cart.set(id, {
                    id: p.id,
                    name: p.name,
                    price: p.price,
                    qty: 1
                });
            }

            renderCart();
            Swal.fire({
                toast: true,
                position: "top-end",
                icon: "success",
                title: "Added",
                timer: 1200,
                showConfirmButton: false
            });
        });
    });

    document.getElementById("pageInfo").textContent =
        `Page ${data.page + 1} / ${data.totalPages}`;
}

// -----------------------------
// COMPLETE ORDER
// -----------------------------
async function completeOrder() {
    if (cart.size === 0) {
        Swal.fire("Cart empty");
        return;
    }

    const customerName = document.getElementById("customerName").value.trim();
    if (!customerName) {
        Swal.fire("Customer name required");
        return;
    }

    const payload = {
        customerName,
        phone: document.getElementById("phone").value,
        address: document.getElementById("address").value,
        paymentMethod: document.getElementById("paymentMethod").value,
        extraDiscount: discountValue(),
        items: Array.from(cart.values()).map(it => ({
            productId: it.id,
            qty: it.qty
        }))
    };

    const res = await fetch("/admin/pos/checkout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        Swal.fire("Checkout failed");
        return;
    }

    const data = await res.json();

    Swal.fire({
        icon: "success",
        title: "Order completed",
        text: `Order: ${data.orderNo}`
    });

    cart.clear();
    renderCart();
}

// -----------------------------
// INIT
// -----------------------------
document.addEventListener("DOMContentLoaded", () => {

    loadProducts();
    renderCart();

    document.getElementById("btnSearch").addEventListener("click", () => {
        q = document.getElementById("q").value;
        page = 0;
        loadProducts();
    });

    document.getElementById("btnComplete")
        .addEventListener("click", completeOrder);

    document.getElementById("btnClear")
        .addEventListener("click", () => {
            cart.clear();
            renderCart();
        });

    document.getElementById("extraDiscount")
        .addEventListener("input", updateSummary);
});
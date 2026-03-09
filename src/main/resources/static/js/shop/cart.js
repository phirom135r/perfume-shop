(function () {
    const cartCountBadge = document.getElementById("cartCountBadge");
    const summaryCartCount = document.getElementById("summaryCartCount");
    const summarySubtotal = document.getElementById("summarySubtotal");
    const cartItemsWrap = document.getElementById("cartItemsWrap");

    function csrfHeaders() {
        const headers = {
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "application/json"
        };

        if (window.shopCartConfig && window.shopCartConfig.csrfHeader && window.shopCartConfig.csrfToken) {
            headers[window.shopCartConfig.csrfHeader] = window.shopCartConfig.csrfToken;
        }

        return headers;
    }

    function toastSuccess(title) {
        Swal.fire({
            toast: true,
            position: "top-end",
            icon: "success",
            title,
            showConfirmButton: false,
            timer: 1200,
            timerProgressBar: true
        });
    }

    function toastError(title) {
        Swal.fire({
            toast: true,
            position: "top-end",
            icon: "error",
            title,
            showConfirmButton: false,
            timer: 1800,
            timerProgressBar: true
        });
    }

    function syncSummary(cartCount, subtotal) {
        if (cartCountBadge) cartCountBadge.textContent = cartCount ?? 0;
        if (summaryCartCount) summaryCartCount.textContent = cartCount ?? 0;
        if (summarySubtotal) summarySubtotal.textContent = subtotal ?? "$0.00";
    }

    function renderEmptyState() {
        if (!cartItemsWrap) return;

        cartItemsWrap.innerHTML = `
            <div class="empty-state" id="emptyCartState">
                <i class="bi bi-cart-x"></i>
                <h5 class="mb-2">Your cart is empty</h5>
                <p class="mb-3">Add some perfumes and come back here.</p>
                <a href="/perfume-shop" class="btn btn-gold rounded-pill px-4">Shop Now</a>
            </div>
        `;
    }

    async function postForm(url, data) {
        const body = new URLSearchParams();
        Object.keys(data).forEach(key => body.append(key, data[key]));

        const res = await fetch(url, {
            method: "POST",
            headers: csrfHeaders(),
            body: body.toString()
        });

        return await res.json();
    }

    async function updateQty(productId, qty) {
        try {
            const json = await postForm("/perfume-shop/cart/update", { productId, qty });

            if (!json.ok) {
                toastError(json.message || "Cannot update cart");
                return;
            }

            const qtyInput = document.querySelector(`.qty-input[data-id="${productId}"]`);
            const lineTotal = document.getElementById(`line-total-${productId}`);

            if (qtyInput) qtyInput.value = json.qty ?? qty;
            if (lineTotal) lineTotal.textContent = `$${json.lineTotal ?? "0.00"}`;

            syncSummary(json.cartCount, json.subtotal);
        } catch (e) {
            toastError("Cannot update cart");
        }
    }

    async function removeItem(productId) {
        const ok = await Swal.fire({
            title: "Remove this item?",
            text: "This product will be removed from your cart.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Yes, remove",
            cancelButtonText: "Cancel"
        });

        if (!ok.isConfirmed) return;

        try {
            const json = await postForm("/perfume-shop/cart/remove", { productId });

            if (!json.ok) {
                toastError(json.message || "Cannot remove item");
                return;
            }

            const row = document.getElementById(`cart-row-${productId}`);
            if (row) row.remove();

            syncSummary(json.cartCount, json.subtotal);

            const remainingRows = document.querySelectorAll(".cart-row");
            if (remainingRows.length === 0) {
                renderEmptyState();
            }

            toastSuccess("Item removed");
        } catch (e) {
            toastError("Cannot remove item");
        }
    }

    async function clearCart() {
        const ok = await Swal.fire({
            title: "Clear cart?",
            text: "All items will be removed from your cart.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Yes, clear",
            cancelButtonText: "Cancel"
        });

        if (!ok.isConfirmed) return;

        try {
            const json = await postForm("/perfume-shop/cart/clear", {});

            if (!json.ok) {
                toastError(json.message || "Cannot clear cart");
                return;
            }

            syncSummary(0, "$0.00");
            renderEmptyState();
            toastSuccess("Cart cleared");
        } catch (e) {
            toastError("Cannot clear cart");
        }
    }

    document.addEventListener("click", function (e) {
        const incBtn = e.target.closest(".btn-inc");
        const decBtn = e.target.closest(".btn-dec");
        const removeBtn = e.target.closest(".btn-remove");
        const clearBtn = e.target.closest("#btnClearCart");

        if (incBtn) {
            const productId = incBtn.dataset.id;
            const stock = Number(incBtn.dataset.stock || 0);
            const qtyInput = document.querySelector(`.qty-input[data-id="${productId}"]`);
            const currentQty = Number(qtyInput?.value || 1);

            if (stock > 0 && currentQty >= stock) {
                toastError("Stock limit reached");
                return;
            }

            updateQty(productId, currentQty + 1);
        }

        if (decBtn) {
            const productId = decBtn.dataset.id;
            const qtyInput = document.querySelector(`.qty-input[data-id="${productId}"]`);
            const currentQty = Number(qtyInput?.value || 1);

            if (currentQty <= 1) {
                removeItem(productId);
                return;
            }

            updateQty(productId, currentQty - 1);
        }

        if (removeBtn) {
            const productId = removeBtn.dataset.id;
            removeItem(productId);
        }

        if (clearBtn) {
            clearCart();
        }
    });

    document.addEventListener("change", function (e) {
        const qtyInput = e.target.closest(".qty-input");
        if (!qtyInput) return;

        const productId = qtyInput.dataset.id;
        const stock = Number(qtyInput.dataset.stock || 0);
        let qty = Number(qtyInput.value || 1);

        if (qty < 1) qty = 1;
        if (stock > 0 && qty > stock) qty = stock;

        qtyInput.value = qty;
        updateQty(productId, qty);
    });
})();
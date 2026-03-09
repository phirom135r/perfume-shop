(function () {

    const cartCountBadge = document.getElementById("cartCountBadge");
    const summaryItemCount = document.getElementById("summaryItemCount");
    const subtotalAmount = document.getElementById("subtotalAmount");
    const cartItemsWrap = document.getElementById("cartItemsWrap");
    const clearCartBtn = document.getElementById("clearCartBtn");
    const emptyCartState = document.getElementById("emptyCartState");
    const cartContentWrapper = document.getElementById("cartContentWrapper");

    function toastSuccess(title) {
        Swal.fire({
            toast: true,
            position: "top-end",
            icon: "success",
            title,
            showConfirmButton: false,
            timer: 1300,
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

    function updateSummary(cartCount, subtotal) {
        if (cartCountBadge) cartCountBadge.textContent = cartCount ?? 0;
        if (summaryItemCount) summaryItemCount.textContent = cartCount ?? 0;
        if (subtotalAmount) subtotalAmount.textContent = subtotal ?? "$0.00";
    }

    function toggleEmptyState() {
        const hasItems = cartItemsWrap && cartItemsWrap.querySelector(".cart-row");

        if (!hasItems) {
            if (emptyCartState) emptyCartState.style.display = "";
            if (cartContentWrapper) cartContentWrapper.style.display = "none";
        } else {
            if (emptyCartState) emptyCartState.style.display = "none";
            if (cartContentWrapper) cartContentWrapper.style.display = "";
        }
    }

    async function postFormUrlEncoded(url, data) {
        const body = new URLSearchParams();
        Object.keys(data).forEach(k => body.append(k, data[k]));

        const res = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Accept": "application/json"
            },
            body: body.toString()
        });

        return await res.json();
    }

    async function removeItem(productId, rowEl) {
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
            const json = await postFormUrlEncoded("/perfume-shop/cart/remove", { productId });

            if (!json.ok) {
                toastError(json.message || "Cannot remove item");
                return;
            }

            if (rowEl) {
                rowEl.classList.add("removing");
                setTimeout(() => {
                    rowEl.remove();
                    toggleEmptyState();
                }, 250);
            }

            updateSummary(json.cartCount, json.subtotal);
            toastSuccess("Item removed");
        } catch (e) {
            toastError("Cannot remove item");
        }
    }

    async function clearCart() {
        const ok = await Swal.fire({
            title: "Clear all cart items?",
            text: "All products in your cart will be removed.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Yes, clear cart",
            cancelButtonText: "Cancel"
        });

        if (!ok.isConfirmed) return;

        try {
            const res = await fetch("/perfume-shop/cart/clear", {
                method: "POST",
                headers: { "Accept": "application/json" }
            });

            const json = await res.json();

            if (!json.ok) {
                toastError(json.message || "Cannot clear cart");
                return;
            }

            if (cartItemsWrap) cartItemsWrap.innerHTML = "";
            updateSummary(0, "$0.00");
            toggleEmptyState();
            toastSuccess("Cart cleared");
        } catch (e) {
            toastError("Cannot clear cart");
        }
    }

    async function updateQty(productId, qty, rowEl, inputEl) {
        try {
            const json = await postFormUrlEncoded("/perfume-shop/cart/update", { productId, qty });

            if (!json.ok) {
                toastError(json.message || "Cannot update quantity");
                return;
            }

            if (inputEl && json.qty != null) {
                inputEl.value = json.qty;
            }

            if (rowEl && json.lineTotal != null) {
                const lineTotalEl = rowEl.querySelector(".line-total");
                if (lineTotalEl) lineTotalEl.textContent = json.lineTotal;
            }

            updateSummary(json.cartCount, json.subtotal);
            toastSuccess("Cart updated");
        } catch (e) {
            toastError("Cannot update quantity");
        }
    }

    document.addEventListener("click", function (e) {
        const removeBtn = e.target.closest(".remove-item-btn");
        if (removeBtn) {
            const productId = removeBtn.dataset.id;
            const rowEl = removeBtn.closest(".cart-row");
            removeItem(productId, rowEl);
            return;
        }

        const decBtn = e.target.closest(".btn-qty-dec");
        if (decBtn) {
            const productId = decBtn.dataset.id;
            const rowEl = decBtn.closest(".cart-row");
            const inputEl = rowEl.querySelector(".qty-input");
            let qty = Number(inputEl.value || 1);
            qty = Math.max(1, qty - 1);
            updateQty(productId, qty, rowEl, inputEl);
            return;
        }

        const incBtn = e.target.closest(".btn-qty-inc");
        if (incBtn) {
            const productId = incBtn.dataset.id;
            const rowEl = incBtn.closest(".cart-row");
            const inputEl = rowEl.querySelector(".qty-input");
            const max = Number(inputEl.getAttribute("max") || 999999);
            let qty = Number(inputEl.value || 1);
            qty = Math.min(max, qty + 1);
            updateQty(productId, qty, rowEl, inputEl);
        }
    });

    document.addEventListener("change", function (e) {
        const inputEl = e.target.closest(".qty-input");
        if (!inputEl) return;

        const productId = inputEl.dataset.id;
        const rowEl = inputEl.closest(".cart-row");
        const max = Number(inputEl.getAttribute("max") || 999999);

        let qty = Number(inputEl.value || 1);
        if (!Number.isFinite(qty) || qty < 1) qty = 1;
        if (qty > max) qty = max;

        updateQty(productId, qty, rowEl, inputEl);
    });

    if (clearCartBtn) {
        clearCartBtn.addEventListener("click", clearCart);
    }

    toggleEmptyState();

})();
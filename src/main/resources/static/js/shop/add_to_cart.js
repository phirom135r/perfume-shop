(function () {
    const modal = document.getElementById("productDetailModal");
    const modalImage = document.getElementById("modalImage");
    const modalName = document.getElementById("modalName");
    const modalSize = document.getElementById("modalSize");
    const modalPrice = document.getElementById("modalPrice");
    const modalDescription = document.getElementById("modalDescription");
    const modalNotes = document.getElementById("modalNotes");
    const modalAddToCart = document.getElementById("modalAddToCart");
    const cartCountBadge = document.getElementById("cartCountBadge");

    const bsModal = modal ? new bootstrap.Modal(modal) : null;

    const guestAuthModalEl = document.getElementById("guestAuthModal");
    const guestAuthModal = guestAuthModalEl ? new bootstrap.Modal(guestAuthModalEl) : null;

    const customerLoggedIn = !!(window.shopAuth && window.shopAuth.customerLoggedIn);
    let autoPopupShown = false;

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

    function showLoginRequiredModal() {
        if (guestAuthModal) {
            guestAuthModal.show();
        }
    }

    async function addToCart(productId, productName) {
        try {
            const body = new URLSearchParams();
            body.append("productId", productId);
            body.append("qty", "1");

            const headers = {
                "Content-Type": "application/x-www-form-urlencoded",
                "Accept": "application/json"
            };

            if (window.shopAuth && window.shopAuth.csrfHeader && window.shopAuth.csrfToken) {
                headers[window.shopAuth.csrfHeader] = window.shopAuth.csrfToken;
            }

            const res = await fetch("/perfume-shop/cart/add", {
                method: "POST",
                headers,
                body: body.toString()
            });

            const json = await res.json();

            if (!json.ok) {
                if (json.loginRequired) {
                    showLoginRequiredModal();   // ✅ show popup login/register
                    return;
                }

                toastError(json.message || "Cannot add to cart");
                return;
            }

            if (cartCountBadge) {
                cartCountBadge.textContent = json.cartCount ?? 0;
            }

            toastSuccess(productName + " added to cart");
        } catch (e) {
            toastError("Cannot add to cart");
        }
    }

    document.querySelectorAll(".view-detail").forEach(btn => {
        btn.addEventListener("click", function (e) {
            e.preventDefault();

            const name = this.dataset.name || "";
            const price = this.dataset.price || "0.00";
            const description = this.dataset.description || "";
            const image = this.dataset.image || "";
            const notes = this.dataset.notes || "";
            const size = this.dataset.size || "";
            const productId = this.dataset.id || "";

            if (modalImage) {
                modalImage.src = image;
                modalImage.alt = name;
            }
            if (modalName) modalName.textContent = name;
            if (modalSize) modalSize.textContent = size;
            if (modalPrice) modalPrice.textContent = `$${price}`;
            if (modalDescription) modalDescription.textContent = description;
            if (modalNotes) modalNotes.textContent = notes;

            if (modalAddToCart) {
                modalAddToCart.onclick = function () {
                    addToCart(productId, name);
                };
            }

            if (bsModal) bsModal.show();
        });
    });

    document.querySelectorAll(".btn-add-cart").forEach(btn => {
        btn.addEventListener("click", function (e) {
            e.preventDefault();
            const productId = this.dataset.id;
            const name = this.dataset.name || "Product";
            addToCart(productId, name);
        });
    });

    if (!customerLoggedIn) {
        setTimeout(() => {
            if (!autoPopupShown) {
                autoPopupShown = true;
                showLoginRequiredModal();
            }
        }, 10000);
    }
})();
(function () {
    "use strict";

    // ---- Quick debug: check file loaded ----
    console.log("admin-products.js loaded ✅");

    function byId(id) {
        return document.getElementById(id);
    }

    // DOM refs
    const btnAdd = byId("btnAdd");
    const btnReload = byId("btnReload");
    const modalEl = byId("productModal");

    // Form fields
    const form = byId("productForm");
    const f_id = byId("id");
    const f_name = byId("name");
    const f_brand = byId("brand");
    const f_categoryId = byId("categoryId");
    const f_price = byId("price");
    const f_discount = byId("discount");
    const f_stock = byId("stock");
    const f_description = byId("description");
    const f_image = byId("image");
    const preview = byId("preview");

    // Table refs
    const tbody = byId("tbody");

    // ---- Validate required elements exist ----
    if (!btnAdd) console.error("❌ #btnAdd not found");
    if (!modalEl) console.error("❌ #productModal not found");
    if (!form) console.error("❌ #productForm not found");

    // ---- Bootstrap modal instance ----
    let modal;
    try {
        modal = new bootstrap.Modal(modalEl);
    } catch (e) {
        console.error("❌ Bootstrap Modal not available. Did you include bootstrap.bundle.min.js ?", e);
    }

    // ===== Helpers =====
    function resetForm() {
        f_id.value = "";
        f_name.value = "";
        f_brand.value = "";
        f_price.value = "";
        f_discount.value = "0";
        f_stock.value = "";
        f_description.value = "";
        if (f_image) f_image.value = "";

        if (preview) {
            preview.src = "";
            preview.style.display = "none";
        }
    }

    // ===== Add button: open modal =====
    if (btnAdd) {
        btnAdd.addEventListener("click", function () {
            console.log("btnAdd clicked ✅");

            resetForm();
            const modalTitle = byId("modalTitle");
            if (modalTitle) modalTitle.textContent = "Add Product";

            if (modal) modal.show();
        });
    }

    // ===== Image preview =====
    if (f_image && preview) {
        f_image.addEventListener("change", function () {
            const file = f_image.files && f_image.files[0];
            if (!file) {
                preview.style.display = "none";
                return;
            }
            preview.src = URL.createObjectURL(file);
            preview.style.display = "block";
        });
    }

    // ===== Reload button example (later you will implement AJAX load) =====
    if (btnReload) {
        btnReload.addEventListener("click", function () {
            console.log("Reload clicked ✅");
            // TODO: loadProducts()
        });
    }

    // ===== Submit form (later hook to backend save API) =====
    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();
            console.log("Submit clicked ✅");

            // TODO: AJAX save
            // For now just close:
            if (modal) modal.hide();
            alert("Save clicked (AJAX will be added next)");
        });
    }

    // ===== Render demo row (optional) =====
    if (tbody) {
        tbody.innerHTML = `
      <tr>
        <td>1</td>
        <td><span class="text-muted small">no img</span></td>
        <td>Demo</td>
        <td>Brand</td>
        <td>Category</td>
        <td>$10.00</td>
        <td>$0.00</td>
        <td>5</td>
        <td><button class="btn btn-sm btn-outline-secondary">Edit</button></td>
      </tr>
    `;
    }

})();
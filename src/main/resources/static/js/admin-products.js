// admin-products.js
let modal;
let dt;
let _previewUrl = null;

$(document).ready(function () {

    modal = new bootstrap.Modal(document.getElementById("productModal"));

    $("#btnAdd").on("click", openAdd);
    $("#productForm").on("submit", saveProduct);
    $("#image").on("change", previewImage);

    initDataTable();
});

function initDataTable() {
    dt = new DataTable("#tblProducts", {
        processing: true,
        serverSide: true,
        searching: true,
        lengthMenu: [10, 25, 50, 100],
        pageLength: 50,
        ajax: {
            url: "/admin/api/products/dt",
            type: "GET"
        },
        columns: [
            { data: "id" },
            { data: "name" },
            { data: "category" },
            { data: "brand" }, // show brand name
            { data: "stock" },
            {
                data: "price",
                render: function (data) {
                    const v = Number(data || 0).toFixed(2);
                    return "$" + v;
                }
            },
            {
                data: "image",
                orderable: false,
                render: function (data) {
                    if (!data) return "-";
                    return `<img class="thumb" src="${data}" alt="img">`;
                }
            },
            {
                data: "active",
                render: function (data) {
                    if (data === true) return `<span class="status-pill pill-active">active</span>`;
                    return `<span class="status-pill pill-inactive">inactive</span>`;
                }
            },
            { data: "createdAt" },
            {
                data: null,
                orderable: false,
                className: "col-action",
                render: function (row) {
                    const safe = encodeJson(row);
                    return `
                      <div class="action-wrap">
                        <button type="button" class="btn btn-sm btn-primary" onclick='openEdit(${safe})'>
                          <i class="bi bi-pencil-square"></i> Edit
                        </button>
                        <button type="button" class="btn btn-sm btn-danger" onclick="deleteProduct(${row.id})">
                          <i class="bi bi-trash"></i> Delete
                        </button>
                      </div>
                    `;
                }
            }
        ]
    });
}

/* ================= BRAND DROPDOWN ================= */

function loadBrandDropdown(selectedId = null) {
    fetch("/admin/api/brands/active")
        .then(r => r.json())
        .then(list => {
            let html = `<option value="">-- Select Brand --</option>`;
            list.forEach(b => {
                const sel = (selectedId != null && Number(selectedId) === b.id) ? "selected" : "";
                html += `<option value="${b.id}" ${sel}>${b.name}</option>`;
            });
            $("#brandId").html(html);
        });
}

/* ================= MODAL ================= */

function openAdd() {
    $("#modalTitle").text("Add Product");
    $("#productForm")[0].reset();
    $("#id").val("");
    $("#active").val("true");
    setUploadBoxEmpty();

    loadBrandDropdown(null); // ✅ load brands
    modal.show();
}

function openEdit(row) {
    $("#modalTitle").text("Edit Product");

    $("#id").val(row.id);
    $("#name").val(row.name || "");
    $("#stock").val(row.stock ?? 0);
    $("#price").val(row.price ?? 0);
    $("#discount").val(row.discount ?? 0);
    $("#description").val(row.description || "");
    $("#active").val(String(row.active));

    // categoryId + brandId must exist in DTO
    $("#categoryId").val(row.categoryId || "");
    loadBrandDropdown(row.brandId); // ✅
    $("#brandId").val(row.brandId || "");

    if (row.image) {
        $("#uploadPreview").html(`<img src="${row.image}" alt="preview">`);
    } else {
        setUploadBoxEmpty();
    }

    $("#image").val("");
    modal.show();
}

/* ================= SAVE ================= */

function saveProduct(e) {
    e.preventDefault();

    const fd = new FormData();

    const id = $("#id").val();
    if (id) fd.append("id", id);

    fd.append("name", $("#name").val());
    fd.append("brandId", $("#brandId").val());          // ✅ NEW
    fd.append("categoryId", $("#categoryId").val());
    fd.append("price", $("#price").val() || 0);
    fd.append("discount", $("#discount").val() || 0);
    fd.append("stock", $("#stock").val() || 0);
    fd.append("description", $("#description").val() || "");
    fd.append("active", $("#active").val());

    const file = $("#image")[0].files[0];
    if (file) fd.append("image", file);

    fetch("/admin/api/products", { method: "POST", body: fd })
        .then(async (r) => {
            if (!r.ok) throw new Error(await r.text());
            return r.json();
        })
        .then(() => {
            modal.hide();
            toastSuccess("Saved successfully");
            dt.ajax.reload(null, false);
        })
        .catch((err) => toastError(err.message || "Cannot save product"));
}

/* ================= DELETE ================= */

function deleteProduct(id) {
    Swal.fire({
        title: "Delete this product?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Yes, delete",
        cancelButtonText: "Cancel"
    }).then(res => {
        if (!res.isConfirmed) return;

        fetch("/admin/api/products/" + id, { method: "DELETE" })
            .then(r => {
                if (!r.ok) throw new Error("Delete failed");
                toastSuccess("Deleted successfully");
                dt.ajax.reload(null, false);
            })
            .catch(() => toastError("Cannot delete product"));
    });
}

/* ================= PREVIEW ================= */

function previewImage() {
    const file = $("#image")[0].files[0];
    if (!file) return;

    if (_previewUrl) URL.revokeObjectURL(_previewUrl);
    _previewUrl = URL.createObjectURL(file);

    $("#uploadPreview").html(`<img src="${_previewUrl}" alt="preview">`);
}

function setUploadBoxEmpty() {
    $("#uploadPreview").html(`
      <div class="upload-placeholder">
        <div class="plus">+</div>
        <div class="small text-muted">Upload</div>
      </div>
    `);
}

/* ================= TOAST ================= */

function toastSuccess(msg) {
    Swal.fire({ toast: true, position: "top-end", icon: "success", title: msg, showConfirmButton: false, timer: 1500 });
}
function toastError(msg) {
    Swal.fire({ toast: true, position: "top-end", icon: "error", title: msg, showConfirmButton: false, timer: 2200 });
}

/* ================= HELPERS ================= */

function encodeJson(obj) {
    return JSON.stringify(obj).replaceAll("'", "\\'");
}
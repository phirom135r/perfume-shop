let modal;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("brandModal");
    if (modalEl && window.bootstrap) {
        modal = new bootstrap.Modal(modalEl);
    }

    document.getElementById("btnAdd")?.addEventListener("click", openAdd);
    document.getElementById("brandForm")?.addEventListener("submit", save);

    loadData();
});

// ================================
// LOAD DATA
// ================================
function loadData() {
    fetch("/admin/api/brands", { headers: { "Accept": "application/json" } })
        .then(r => {
            if (!r.ok) throw new Error("Cannot load brands");
            return r.json();
        })
        .then(list => {
            let html = "";

            if (!Array.isArray(list) || list.length === 0) {
                html = `<tr><td colspan="4" class="text-center text-muted py-4">No brands found.</td></tr>`;
                document.getElementById("tbody").innerHTML = html;
                return;
            }

            list.forEach(b => {
                const isActive = (b.active === true);

                html += `
                <tr>
                  <td class="fw-semibold">${b.id ?? ""}</td>

                  <td>${escapeHtml(b.name)}</td>

                  <td>
                    ${isActive
                    ? `<span class="status-pill pill-active"><span class="dot"></span>Active</span>`
                    : `<span class="status-pill pill-inactive"><span class="dot"></span>Inactive</span>`
                }
                  </td>

                  <td >
                    <button class="btn btn-sm btn-info me-1"
                            data-id="${b.id}"
                            data-name="${escapeAttr(b.name)}"
                            data-active="${String(isActive)}"
                             onclick="openEdit(this)"><i class="bi bi-pencil-square"></i>
                      Edit
                    </button>

                    <button class="btn btn-sm btn-danger"
                            onclick="removeBrand(${b.id})"><i class="bi bi-trash"></i> 
                      Delete
                    </button>
                  </td>
                </tr>`;
            });

            document.getElementById("tbody").innerHTML = html;
        })
        .catch(() => {
            document.getElementById("tbody").innerHTML =
                `<tr><td colspan="4" class="text-center text-danger py-4">Failed to load brands</td></tr>`;
        });
}

// ================================
// ADD
// ================================
function openAdd() {
    document.getElementById("modalTitle").innerText = "Add Brand";
    document.getElementById("id").value = "";
    document.getElementById("name").value = "";
    document.getElementById("active").value = "true";
    modal?.show();
}

// ================================
// EDIT
// ================================
function openEdit(btn) {
    document.getElementById("modalTitle").innerText = "Edit Brand";
    document.getElementById("id").value = btn.getAttribute("data-id") || "";
    document.getElementById("name").value = btn.getAttribute("data-name") || "";
    document.getElementById("active").value = (btn.getAttribute("data-active") === "true") ? "true" : "false";
    modal?.show();
}

// ================================
// SAVE
// ================================
function save(e) {
    e.preventDefault();

    const idVal = document.getElementById("id").value;
    const name = (document.getElementById("name").value || "").trim();
    const active = document.getElementById("active").value === "true";

    if (!name) {
        Swal.fire("Required", "Brand name is required", "warning");
        return;
    }

    const payload = {
        id: idVal ? Number(idVal) : null,
        name: name,
        active: active
    };

    fetch("/admin/api/brands", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async r => {
            if (!r.ok) {
                const msg = await r.text();
                throw new Error(msg || "Save failed");
            }
            return r.json();
        })
        .then(() => {
            modal?.hide();
            loadData();
            toastSuccess("Saved successfully");
        })
        .catch(err => {
            Swal.fire("Error", err.message || "Cannot save brand!", "error");
        });
}

// ================================
// DELETE
// ================================
function removeBrand(id) {
    Swal.fire({
        title: "Delete brand?",
        text: "You cannot undo this action!",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#6c757d",
        confirmButtonText: "Yes, delete it!"
    }).then((result) => {
        if (!result.isConfirmed) return;

        fetch("/admin/api/brands/" + id, { method: "DELETE" })
            .then(r => {
                if (!r.ok) throw new Error("Delete failed");
                loadData();
                toastSuccess("Deleted successfully");
            })
            .catch(() => {
                Swal.fire("Error", "Cannot delete brand!", "error");
            });
    });
}

// ================================
// TOAST
// ================================
function toastSuccess(title) {
    if (!window.Swal) return;
    Swal.fire({
        toast: true,
        position: "top-end",
        icon: "success",
        title,
        showConfirmButton: false,
        timer: 1500,
        timerProgressBar: true
    });
}

// ================================
// ESCAPE
// ================================
function escapeHtml(str) {
    return String(str ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

function escapeAttr(str) {
    return String(str ?? "")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
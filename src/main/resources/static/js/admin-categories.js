let modal;

document.addEventListener("DOMContentLoaded", () => {
    const modalEl = document.getElementById("categoryModal");
    modal = new bootstrap.Modal(modalEl);

    document.getElementById("btnAdd").addEventListener("click", openAdd);
    document.getElementById("categoryForm").addEventListener("submit", save);

    loadData();
});

// ================================
// LOAD DATA
// ================================
function loadData() {
    fetch("/admin/api/categories")
        .then(r => r.json())
        .then(list => {
            let html = "";

            list.forEach(c => {
                const isActive = !!c.active;

                html += `
          <tr>
            <td>${c.id}</td>
            <td>${escapeHtml(c.name)}</td>
           <td>
              ${isActive
                                ? `<span class="status-pill pill-active">
                     <span class="dot"></span>Active
                   </span>`
                                : `<span class="status-pill pill-inactive">
                     <span class="dot"></span>Inactive
                   </span>`
                            }
            </td>
            
            <td>
              <button class="btn btn-sm btn-info me-1"
                      data-id="${c.id}"
                      data-name="${escapeAttr(c.name)}"
                      data-active="${isActive}"
                      onclick="openEdit(this)"><i class="bi bi-pencil-square"></i>
                Edit
              </button>

              <button class="btn btn-sm btn-danger"
                      onclick="removeCategory(${c.id})"><i class="bi bi-trash"></i> 
                Delete
              </button>
            </td>
          </tr>
        `;
            });

            document.getElementById("tbody").innerHTML = html;
        })
        .catch(() => {
            document.getElementById("tbody").innerHTML =
                `<tr><td colspan="4" class="text-center text-muted py-4">Cannot load categories</td></tr>`;
        });
}

// ================================
// ADD
// ================================
function openAdd() {
    document.getElementById("modalTitle").innerText = "Add Category";
    document.getElementById("id").value = "";
    document.getElementById("name").value = "";
    document.getElementById("active").value = "true"; // default

    modal.show();
}

// ================================
// EDIT
// ================================
function openEdit(btn) {
    document.getElementById("modalTitle").innerText = "Edit Category";
    document.getElementById("id").value = btn.getAttribute("data-id");
    document.getElementById("name").value = btn.getAttribute("data-name");

    const activeVal = btn.getAttribute("data-active"); // "true"/"false"
    document.getElementById("active").value = (activeVal === "true") ? "true" : "false";

    modal.show();
}

// ================================
// SAVE
// ================================
function save(e) {
    e.preventDefault();

    const idVal = document.getElementById("id").value;
    const nameVal = document.getElementById("name").value.trim();
    const activeVal = document.getElementById("active").value === "true";

    if (!nameVal) {
        Swal.fire("Error", "Name is required!", "error");
        return;
    }

    const payload = {
        id: idVal ? Number(idVal) : null,
        name: nameVal,
        active: activeVal
    };

    fetch("/admin/api/categories", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (r) => {
            if (!r.ok) throw new Error(await r.text());
            return r.json();
        })
        .then(() => {
            modal.hide();
            loadData();

            Swal.fire({
                toast: true,
                position: "top-end",
                icon: "success",
                title: "Saved successfully",
                showConfirmButton: false,
                timer: 1500,
                timerProgressBar: true
            });
        })
        .catch((err) => {
            Swal.fire({
                icon: "error",
                title: "Error",
                text: err.message || "Cannot save category!"
            });
        });
}

// ================================
// DELETE
// ================================
function removeCategory(id) {
    Swal.fire({
        title: "Delete category?",
        text: "You cannot undo this action!",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#6c757d",
        confirmButtonText: "Yes, delete it!"
    }).then((result) => {
        if (!result.isConfirmed) return;

        fetch("/admin/api/categories/" + id, { method: "DELETE" })
            .then(r => {
                if (!r.ok) throw new Error();
                loadData();

                Swal.fire({
                    toast: true,
                    position: "top-end",
                    icon: "success",
                    title: "Deleted successfully",
                    showConfirmButton: false,
                    timer: 1500,
                    timerProgressBar: true
                });
            })
            .catch(() => {
                Swal.fire({
                    icon: "error",
                    title: "Error",
                    text: "Cannot delete category!"
                });
            });
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
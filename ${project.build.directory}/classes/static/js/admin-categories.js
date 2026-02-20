let modal;

document.addEventListener("DOMContentLoaded", () => {

    const modalEl = document.getElementById("categoryModal");
    modal = new bootstrap.Modal(modalEl);

    document.getElementById("btnAdd")
        .addEventListener("click", openAdd);

    document.getElementById("categoryForm")
        .addEventListener("submit", save);

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
                html += `
                <tr>
                  <td>${c.id}</td>
                  <td>${escapeHtml(c.name)}</td>

                  <td>
                    <button class="btn btn-sm btn-warning me-1"
                            data-id="${c.id}"
                            data-name="${escapeAttr(c.name)}"
                            onclick="openEdit(this)">
                        Edit
                    </button>

                    <button class="btn btn-sm btn-danger"
                            onclick="removeCategory(${c.id})">
                        Delete
                    </button>
                  </td>
                </tr>`;
            });

            document.getElementById("tbody").innerHTML = html;
        });
}


// ================================
// ADD
// ================================
function openAdd() {

    document.getElementById("modalTitle").innerText = "Add Category";
    document.getElementById("id").value = "";
    document.getElementById("name").value = "";

    modal.show();
}


// ================================
// EDIT
// ================================
function openEdit(btn) {

    document.getElementById("modalTitle").innerText = "Edit Category";
    document.getElementById("id").value = btn.getAttribute("data-id");
    document.getElementById("name").value = btn.getAttribute("data-name");

    modal.show();
}


// ================================
// SAVE
// ================================
function save(e) {

    e.preventDefault();

    const idVal = document.getElementById("id").value;

    const payload = {
        id: idVal ? Number(idVal) : null,
        name: document.getElementById("name").value.trim()
    };

    fetch("/admin/api/categories", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(r => {
            if (!r.ok) throw new Error();
            return r.json();
        })
        .then(() => {

            modal.hide();
            loadData();

            // ✅ Toast Success
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
        .catch(() => {

            Swal.fire({
                icon: "error",
                title: "Error",
                text: "Cannot save category!"
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

        fetch("/admin/api/categories/" + id, {
            method: "DELETE"
        })
            .then(r => {
                if (!r.ok) throw new Error();

                loadData();

                // ✅ Toast Delete Success
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
    return (str ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

function escapeAttr(str) {
    return (str ?? "")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
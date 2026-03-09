let modal;
let dt;
let productsCache = [];

document.addEventListener("DOMContentLoaded", () => {

    modal = new bootstrap.Modal(document.getElementById("stockModal"));

    document.getElementById("btnAdd").addEventListener("click", openAdd);
    document.getElementById("stockForm").addEventListener("submit", saveMovement);

    document.getElementById("action").addEventListener("change", onActionChange);
    document.getElementById("productId").addEventListener("change", updateHint);

    initTable();
    loadProducts();
});

function initTable(){
    dt = new DataTable("#tblStock", {
        processing: true,
        serverSide: true,
        searching: true,
        ajax: {
            url: "/admin/api/stock/dt",
            type: "GET"
        },
        order: [[0, "desc"]],
        columns: [
            { data: "id" },
            { data: "productName" },
            {
                data: "action",
                render: (a) => {
                    const v = String(a || "").toUpperCase();
                    if (v === "IN") return `<span class="badge-act b-in">IN</span>`;
                    if (v === "OUT") return `<span class="badge-act b-out">OUT</span>`;
                    return `<span class="badge-act b-adj">ADJUST</span>`;
                }
            },
            { data: "qty",className: "text-center"  },
            { data: "beforeStock",className: "text-center"  },
            { data: "afterStock" , className: "text-center"  },
            { data: "note",className: "text-center"  },
            { data: "createdAt" , className: "text-center"  },
        ]
    });
}

function openAdd(){
    document.getElementById("stockForm").reset();
    document.getElementById("action").value = "IN";
    onActionChange();
    updateHint();
    modal.show();
}

async function loadProducts(){
    const res = await fetch("/admin/api/stock/products");
    const list = await res.json();
    productsCache = list || [];

    let html = `<option value="">-- Select Product --</option>`;
    productsCache.forEach(p => {
        html += `<option value="${p.id}">${escapeHtml(p.name)} (Stock: ${Number(p.stock||0)})</option>`;
    });
    document.getElementById("productId").innerHTML = html;
}

function onActionChange(){
    const a = document.getElementById("action").value;
    const label = document.getElementById("qtyLabel");
    const qty = document.getElementById("qty");

    if (a === "ADJUST") {
        label.textContent = "New Stock";
        qty.min = "0";
        qty.placeholder = "Set stock to ...";
    } else {
        label.textContent = "Qty";
        qty.min = "1";
        qty.placeholder = "Qty ...";
    }
    updateHint();
}

function updateHint(){
    const pid = Number(document.getElementById("productId").value || 0);
    const a = document.getElementById("action").value;
    const p = productsCache.find(x => Number(x.id) === pid);
    const hint = document.getElementById("hint");
    if (!p) { hint.textContent = ""; return; }

    const st = Number(p.stock || 0);

    if (a === "IN") hint.textContent = `Current stock: ${st}. IN will increase stock.`;
    else if (a === "OUT") hint.textContent = `Current stock: ${st}. OUT cannot make negative stock.`;
    else hint.textContent = `Current stock: ${st}. ADJUST will set stock to new value.`;
}

async function saveMovement(e){
    e.preventDefault();

    const payload = {
        productId: Number(document.getElementById("productId").value || 0),
        action: document.getElementById("action").value,
        qty: Number(document.getElementById("qty").value || 0),
        note: document.getElementById("note").value || ""
    };

    try{
        const r = await fetch("/admin/api/stock", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if(!r.ok){
            const msg = await r.text();
            throw new Error(msg || "Save failed");
        }

        modal.hide();
        Swal.fire({ toast:true, position:"top-end", icon:"success", title:"Saved", showConfirmButton:false, timer:1200 });

        dt.ajax.reload(null, false);
        await loadProducts(); // refresh stock in dropdown

    }catch(err){
        Swal.fire("Error", err.message || "Cannot save", "error");
    }
}

function escapeHtml(s){
    return String(s||"")
        .replaceAll("&","&amp;")
        .replaceAll("<","&lt;")
        .replaceAll(">","&gt;")
        .replaceAll('"',"&quot;")
        .replaceAll("'","&#039;");
}
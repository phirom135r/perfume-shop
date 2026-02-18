let currentPage = 0;

function fmtMoney(v){
    return "$" + Number(v).toFixed(2);
}

function buildRow(p){
    const img = p.image ? `<img src="${p.image}" style="height:40px" class="rounded">` : "-";
    const badge = p.status === "ACTIVE"
        ? `<span class="badge bg-success">active</span>`
        : `<span class="badge bg-secondary">inactive</span>`;

    return `
    <tr>
      <td>${p.id}</td>
      <td>${p.name}</td>
      <td>${p.category}</td>
      <td>${p.stock}</td>
      <td>${fmtMoney(p.price)}</td>
      <td>${img}</td>
      <td>${badge}</td>
      <td>
        <button class="btn btn-sm btn-primary me-1" onclick="openEdit(${encodeURIComponent(JSON.stringify(p))})">Edit</button>
        <button class="btn btn-sm btn-danger" onclick="del(${p.id})">Delete</button>
      </td>
    </tr>
  `;
}

function renderPagination(totalPages, page){
    const ul = $("#pagination");
    ul.empty();
    const prevDisabled = page <= 0 ? "disabled" : "";
    ul.append(`<li class="page-item ${prevDisabled}"><a class="page-link" href="#" onclick="gotoPage(${page-1})">Prev</a></li>`);

    for(let i=0;i<totalPages;i++){
        const active = i === page ? "active" : "";
        ul.append(`<li class="page-item ${active}"><a class="page-link" href="#" onclick="gotoPage(${i})">${i+1}</a></li>`);
    }

    const nextDisabled = page >= totalPages-1 ? "disabled" : "";
    ul.append(`<li class="page-item ${nextDisabled}"><a class="page-link" href="#" onclick="gotoPage(${page+1})">Next</a></li>`);
}

function load(){
    const q = $("#search").val();
    const size = $("#size").val();
    const categoryId = $("#categoryFilter").val();

    $.get("/api/products", { page: currentPage, size, q, categoryId }, function(res){
        if(!res.success){
            Swal.fire("Error", res.message, "error");
            return;
        }
        const data = res.data;
        $("#tbody").html(data.items.map(buildRow).join(""));
        $("#pageInfo").text(`Page ${data.page+1} / ${data.totalPages}  (Total: ${data.totalItems})`);
        renderPagination(data.totalPages, data.page);
    });
}

function gotoPage(p){
    if(p < 0) return;
    currentPage = p;
    load();
}

function resetForm(){
    $("#id").val("");
    $("#name").val("");
    $("#description").val("");
    $("#price").val("");
    $("#costPrice").val("0");
    $("#stock").val("0");
    $("#status").val("ACTIVE");
    $("#image").val("");
    $("#preview").hide().attr("src","");
}

function openAdd(){
    resetForm();
    $("#modalTitle").text("Add Product");
    new bootstrap.Modal(document.getElementById("productModal")).show();
}

function openEdit(p){
    // p is object already (we passed JSON)
    $("#modalTitle").text("Edit Product");
    $("#id").val(p.id);
    $("#name").val(p.name);
    $("#description").val(p.description || "");
    $("#price").val(p.price);
    $("#costPrice").val(p.costPrice || 0);
    $("#stock").val(p.stock);
    $("#status").val(p.status);
    // categoryId can't be set from API row; keep filter list or add it in API if you want

    if(p.image){
        $("#preview").show().attr("src", p.image);
    } else {
        $("#preview").hide();
    }

    new bootstrap.Modal(document.getElementById("productModal")).show();
}

function del(id){
    Swal.fire({
        title: "Delete this product?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Yes, delete"
    }).then((r)=>{
        if(!r.isConfirmed) return;
        $.ajax({
            url: "/api/products/" + id,
            type: "DELETE",
            success: function(res){
                if(res.success){
                    Swal.fire("Deleted", res.message, "success");
                    load();
                }else{
                    Swal.fire("Error", res.message, "error");
                }
            }
        });
    });
}

$(function(){
    $("#btnAdd").click(openAdd);
    $("#btnReload").click(()=>{ currentPage=0; load(); });
    $("#search").on("input", ()=>{ currentPage=0; load(); });
    $("#size, #categoryFilter").change(()=>{ currentPage=0; load(); });

    $("#image").change(function(){
        const f = this.files?.[0];
        if(!f) return;
        const url = URL.createObjectURL(f);
        $("#preview").show().attr("src", url);
    });

    $("#productForm").submit(function(e){
        e.preventDefault();

        const id = $("#id").val();
        const data = {
            categoryId: Number($("#categoryId").val()),
            name: $("#name").val(),
            description: $("#description").val(),
            price: Number($("#price").val()),
            costPrice: Number($("#costPrice").val()),
            stock: Number($("#stock").val()),
            status: $("#status").val()
        };

        const fd = new FormData();
        fd.append("data", new Blob([JSON.stringify(data)], {type: "application/json"}));
        const img = $("#image")[0].files?.[0];
        if(img) fd.append("image", img);

        const method = id ? "PUT" : "POST";
        const url = id ? "/api/products/" + id : "/api/products";

        $.ajax({
            url,
            method,
            data: fd,
            processData: false,
            contentType: false,
            success: function(res){
                if(res.success){
                    Swal.fire("Success", res.message, "success");
                    bootstrap.Modal.getInstance(document.getElementById("productModal")).hide();
                    load();
                } else {
                    Swal.fire("Error", res.message, "error");
                }
            },
            error: function(xhr){
                Swal.fire("Error", xhr.responseText || "Request failed", "error");
            }
        });
    });

    load();
});

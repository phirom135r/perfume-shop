let modal;
let _previewUrl = null;

document.addEventListener("DOMContentLoaded", () => {

    modal = new bootstrap.Modal(document.getElementById("productModal"));

    document.getElementById("btnAdd").addEventListener("click", openAdd);
    document.getElementById("productForm").addEventListener("submit", saveProduct);
    document.getElementById("image").addEventListener("change", previewImage);

    loadProducts();
});

/* ================= TOAST ================= */

function toastSuccess(msg){
    Swal.fire({
        toast:true,
        position:"top-end",
        icon:"success",
        title:msg,
        showConfirmButton:false,
        timer:1500
    });
}

function toastError(msg){
    Swal.fire({
        toast:true,
        position:"top-end",
        icon:"error",
        title:msg,
        showConfirmButton:false,
        timer:1800
    });
}

/* ================= LOAD ================= */

function loadProducts(){
    fetch("/admin/api/products")
        .then(r=>r.json())
        .then(list=>{
            let html="";
            list.forEach(p=>{

                const img = p.image
                    ? `<img src="${p.image}" style="height:46px;border-radius:8px;object-fit:cover;">`
                    : "-";

                html += `
                <tr>
                  <td>${p.id}</td>
                  <td>${img}</td>
                  <td>${escapeHtml(p.name)}</td>
                  <td>${escapeHtml(p.brand||"")}</td>
                  <td>${Number(p.price||0).toFixed(2)}</td>
                  <td>${Number(p.discount||0).toFixed(2)}</td>
                  <td>${p.stock||0}</td>
                  <td>
                    <button class="btn btn-sm btn-warning me-1"
                        onclick='openEdit(${encodeJson(p)})'>Edit</button>
                    <button class="btn btn-sm btn-danger"
                        onclick="deleteProduct(${p.id})">Delete</button>
                  </td>
                </tr>`;
            });

            document.getElementById("tbody").innerHTML = html;
        })
        .catch(()=>toastError("Cannot load products"));
}

/* ================= OPEN ================= */

function openAdd(){

    document.getElementById("modalTitle").innerText="Add Product";

    document.getElementById("productForm").reset();
    document.getElementById("id").value="";

    setUploadBoxEmpty();

    modal.show();
}

function openEdit(p){

    document.getElementById("modalTitle").innerText="Edit Product";

    document.getElementById("id").value=p.id;
    document.getElementById("name").value=p.name||"";
    document.getElementById("brand").value=p.brand||"";
    document.getElementById("price").value=p.price||0;
    document.getElementById("discount").value=p.discount||0;
    document.getElementById("stock").value=p.stock||0;
    document.getElementById("description").value=p.description||"";
    document.getElementById("categoryId").value=p.categoryId||"";

    if(p.image){
        document.getElementById("uploadPreview").innerHTML =
            `<img src="${p.image}">`;
    }else{
        setUploadBoxEmpty();
    }

    modal.show();
}

/* ================= SAVE ================= */

function saveProduct(e){
    e.preventDefault();

    const fd=new FormData();

    const id=document.getElementById("id").value;
    if(id) fd.append("id",id);

    fd.append("name",document.getElementById("name").value);
    fd.append("brand",document.getElementById("brand").value);
    fd.append("categoryId",document.getElementById("categoryId").value);
    fd.append("price",document.getElementById("price").value);
    fd.append("discount",document.getElementById("discount").value);
    fd.append("stock",document.getElementById("stock").value);
    fd.append("description",document.getElementById("description").value);

    const file=document.getElementById("image").files[0];
    if(file) fd.append("image",file);

    fetch("/admin/api/products",{
        method:"POST",
        body:fd
    })
        .then(r=>{
            if(!r.ok) throw new Error();
            return r.json();
        })
        .then(()=>{
            modal.hide();
            toastSuccess("Saved successfully");
            loadProducts();
        })
        .catch(()=>toastError("Cannot save product"));
}

/* ================= DELETE ================= */

function deleteProduct(id){

    Swal.fire({
        title:"Delete this product?",
        icon:"warning",
        showCancelButton:true,
        confirmButtonText:"Yes",
        cancelButtonText:"Cancel"
    }).then(res=>{

        if(!res.isConfirmed) return;

        fetch("/admin/api/products/"+id,{method:"DELETE"})
            .then(()=>{

                toastSuccess("Deleted successfully");
                loadProducts();

            })
            .catch(()=>toastError("Cannot delete product"));
    });
}

/* ================= PREVIEW ================= */

function previewImage(){

    const file=document.getElementById("image").files[0];
    if(!file) return;

    if(_previewUrl) URL.revokeObjectURL(_previewUrl);

    _previewUrl=URL.createObjectURL(file);

    document.getElementById("uploadPreview").innerHTML =
        `<img src="${_previewUrl}">`;
}

/* ================= HELPERS ================= */

function setUploadBoxEmpty(){
    document.getElementById("uploadPreview").innerHTML=`
      <div class="upload-placeholder">
        <div class="plus">+</div>
        <div class="small text-muted">Upload</div>
      </div>`;
}

function escapeHtml(str){
    return (str||"")
        .replaceAll("&","&amp;")
        .replaceAll("<","&lt;")
        .replaceAll(">","&gt;");
}

function encodeJson(obj){
    return JSON.stringify(obj).replaceAll("'","\\'");
}
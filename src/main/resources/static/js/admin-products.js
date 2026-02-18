let modal;
let state = {
    page:0,
    size:10,
    q:"",
    categoryId:"",
    active:"",
    sort:"id_desc"
};

document.addEventListener("DOMContentLoaded", ()=>{

    modal = new bootstrap.Modal(document.getElementById("productModal"));

    document.getElementById("btnAdd").onclick=openAdd;
    document.getElementById("productForm").onsubmit=saveProduct;
    document.getElementById("image").onchange=previewImage;

    document.getElementById("btnSearch").onclick=()=>{
        readFilters();
        state.page=0;
        loadProducts();
    };

    loadProducts();
});

/* ================= LOAD ================= */

function readFilters(){
    state.q=document.getElementById("q").value;
    state.categoryId=document.getElementById("filterCategory").value;
    state.active=document.getElementById("filterActive").value;
    state.sort=document.getElementById("sort").value;
}

function buildQuery(){
    const p=new URLSearchParams();

    p.set("page",state.page);
    p.set("size",state.size);
    p.set("sort",state.sort);

    if(state.q) p.set("q",state.q);
    if(state.categoryId) p.set("categoryId",state.categoryId);
    if(state.active!=="") p.set("active",state.active);

    return p.toString();
}

function loadProducts(){

    fetch("/admin/api/products?"+buildQuery())
        .then(r=>r.json())
        .then(data=>{

            let html="";

            data.items.forEach(p=>{

                const img=p.image
                    ? `<img src="${p.image}" style="height:46px;width:46px;border-radius:8px;object-fit:cover;">`
                    : "-";

                const badge=p.active
                    ? `<span class="badge text-bg-success">Active</span>`
                    : `<span class="badge text-bg-secondary">Inactive</span>`;

                html+=`
                <tr>
                    <td>${p.id}</td>
                    <td>${img}</td>
                    <td>${p.name}</td>
                    <td>${p.brand||""}</td>
                    <td>${p.categoryName||""}</td>
                    <td>${p.price}</td>
                    <td>${p.discount}</td>
                    <td>${p.stock}</td>
                    <td>${badge}</td>
                    <td>
                        <button class="btn btn-warning btn-sm"
                            onclick='openEdit(${JSON.stringify(p)})'>Edit</button>

                        <button class="btn btn-danger btn-sm"
                            onclick="deleteProduct(${p.id})">Delete</button>
                    </td>
                </tr>`;
            });

            document.getElementById("tbody").innerHTML=html;

            renderPagination(data.page,data.totalPages);
        });
}

/* ================= PAGINATION ================= */

function renderPagination(page,total){

    let html="";

    for(let i=0;i<total;i++){
        html+=`
        <li class="page-item ${i===page?'active':''}">
            <button class="page-link" onclick="goPage(${i})">${i+1}</button>
        </li>`;
    }

    document.getElementById("pagination").innerHTML=html;
}

function goPage(p){
    state.page=p;
    loadProducts();
}

/* ================= MODAL ================= */

function openAdd(){

    document.getElementById("modalTitle").innerText="Add Product";
    document.getElementById("productForm").reset();
    document.getElementById("id").value="";

    setUploadEmpty();

    modal.show();
}

function openEdit(p){

    document.getElementById("modalTitle").innerText="Edit Product";

    document.getElementById("id").value=p.id;
    document.getElementById("name").value=p.name;
    document.getElementById("brand").value=p.brand;
    document.getElementById("price").value=p.price;
    document.getElementById("discount").value=p.discount;
    document.getElementById("stock").value=p.stock;
    document.getElementById("description").value=p.description;
    document.getElementById("categoryId").value=p.categoryId;

    if(p.image){
        document.getElementById("uploadPreview").innerHTML=
            `<img src="${p.image}">`;
    }else{
        setUploadEmpty();
    }

    modal.show();
}

/* ================= SAVE ================= */

function saveProduct(e){

    e.preventDefault();

    const fd=new FormData();

    if(id.value) fd.append("id",id.value);

    fd.append("name",name.value);
    fd.append("brand",brand.value);
    fd.append("categoryId",categoryId.value);
    fd.append("price",price.value);
    fd.append("discount",discount.value);
    fd.append("stock",stock.value);
    fd.append("description",description.value);

    if(image.files[0]){
        fd.append("image",image.files[0]);
    }

    fetch("/admin/api/products",{method:"POST",body:fd})
        .then(()=>{

            modal.hide();
            Swal.fire({
                toast:true,
                position:"top-end",
                icon:"success",
                title:"Saved successfully",
                showConfirmButton:false,
                timer:1500
            });

            loadProducts();
        });
}

/* ================= DELETE ================= */

function deleteProduct(id){

    Swal.fire({
        title:"Delete this product?",
        icon:"warning",
        showCancelButton:true
    }).then(r=>{

        if(!r.isConfirmed) return;

        fetch("/admin/api/products/"+id,{method:"DELETE"})
            .then(()=>loadProducts());
    });
}

/* ================= IMAGE ================= */

function previewImage(){

    const file=image.files[0];
    if(!file) return;

    const url=URL.createObjectURL(file);

    uploadPreview.innerHTML=`<img src="${url}">`;
}

function setUploadEmpty(){

    uploadPreview.innerHTML=`
    <div class="upload-placeholder">
        <div class="plus">+</div>
        <div class="small text-muted">Upload</div>
    </div>`;
}
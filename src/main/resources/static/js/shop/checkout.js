(() => {
    const checkoutForm = document.getElementById("checkoutForm");
    const btnPlaceOrder = document.getElementById("btnPlaceOrder");
    const csrfInput = checkoutForm.querySelector('input[name="_csrf"]');

    const khqrModalEl = document.getElementById("khqrModal");
    const khqrModal = new bootstrap.Modal(khqrModalEl, {
    backdrop: "static",
    keyboard: false
});

    const khqrCanvasBox = document.getElementById("khqrCanvas");
    const khqrTotal = document.getElementById("khqrTotal");
    const countdownEl = document.getElementById("countdown");
    const paymentStatusText = document.getElementById("paymentStatusText");
    const btnCancelPayment = document.getElementById("btnCancelPayment");
    const btnCloseKhqr = document.getElementById("btnCloseKhqr");

    let currentMd5 = null;
    let currentOrderId = null;
    let pollTimer = null;
    let countdownTimer = null;
    let secondsLeft = 180;

    function toastError(title) {
    Swal.fire({
    toast: true,
    position: "top-end",
    icon: "error",
    title,
    showConfirmButton: false,
    timer: 2200,
    timerProgressBar: true
});
}

    function setLoading(isLoading) {
    btnPlaceOrder.disabled = isLoading;
    btnPlaceOrder.textContent = isLoading ? "Processing..." : "Place Order";
}

    function clearTimers() {
    if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
}
    if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
}
}

    function formatTime(totalSeconds) {
    const min = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
    const sec = String(totalSeconds % 60).padStart(2, "0");
    return `${min}:${sec}`;
}

    function showOrderSuccess(orderId) {
    Swal.fire({
    icon: "success",
    title: "Order Placed Successfully",
    html: `
                <div style="font-size:15px">
                    Thank you for shopping with <b>Fragrance Haven</b>.<br><br>
                    Your order ID is:<br>
                    <b style="font-size:18px">#${orderId}</b>
                </div>
            `,
    showDenyButton: true,
    confirmButtonText: "My Orders",
    denyButtonText: "Continue Shopping",
    confirmButtonColor: "#c7a27a",
    denyButtonColor: "#cccccc",
    reverseButtons: true
}).then((result) => {
    if (result.isConfirmed) {
    window.location.href = "/perfume-shop/my-orders";
} else {
    window.location.href = "/perfume-shop/shop";
}
});
}

    function startCountdown() {
    secondsLeft = 180;
    countdownEl.textContent = formatTime(secondsLeft);

    countdownTimer = setInterval(async () => {
    secondsLeft--;
    countdownEl.textContent = formatTime(secondsLeft);

    if (secondsLeft <= 0) {
    clearTimers();
    paymentStatusText.textContent = "Payment expired.";

    if (currentMd5) {
    await cancelPayment(false);
}
}
}, 1000);
}

    async function openKhqrModal(orderId, total, khqrString, md5) {
    currentOrderId = orderId;
    currentMd5 = md5;

    khqrTotal.textContent = Number(total || 0).toFixed(2);
    paymentStatusText.textContent = "Waiting for payment...";
    khqrCanvasBox.innerHTML = "";

    await QRCode.toCanvas(khqrString, { width: 260 }, function (err, canvas) {
    if (err) throw err;
    khqrCanvasBox.appendChild(canvas);
});

    khqrModal.show();
    startCountdown();
    pollTimer = setInterval(verifyPayment, 5000);
}

    async function verifyPayment() {
    if (!currentMd5) return;

    try {
    const res = await fetch(`/perfume-shop/api/payment/verify?md5=${encodeURIComponent(currentMd5)}`, {
    headers: { "Accept": "application/json" }
});

    const json = await res.json();
    const status = String(json.status || "").toUpperCase();

    if (status === "PAID") {
    clearTimers();
    khqrModal.hide();
    showOrderSuccess(json.orderId || currentOrderId);
} else if (status === "CANCELLED") {
    clearTimers();
    khqrModal.hide();

    await Swal.fire({
    icon: "warning",
    title: "Payment Cancelled"
});

    window.location.href = "/perfume-shop/cart";
} else if (status === "OUT_OF_STOCK") {
    clearTimers();
    khqrModal.hide();

    await Swal.fire({
    icon: "error",
    title: "Out of Stock",
    text: "Some products are no longer available."
});
}
} catch (e) {
    console.error(e);
}
}

    async function cancelPayment(showConfirm = true) {
    if (!currentMd5) return;

    if (showConfirm) {
    const ok = await Swal.fire({
    title: "Cancel payment?",
    text: "This order will be cancelled.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Yes, cancel"
});

    if (!ok.isConfirmed) return;
}

    try {
    const res = await fetch(`/perfume-shop/api/payment/cancel?md5=${encodeURIComponent(currentMd5)}`, {
    method: "POST",
    headers: {
    "Accept": "application/json",
    "X-CSRF-TOKEN": csrfInput ? csrfInput.value : ""
}
});

    const json = await res.json();
    const status = String(json.status || "").toUpperCase();

    clearTimers();
    khqrModal.hide();

    if (status === "CANCELLED") {
    await Swal.fire({
    icon: "info",
    title: "Payment Cancelled"
});

    window.location.href = "/perfume-shop/cart";
} else if (status === "PAID") {
    showOrderSuccess(currentOrderId);
} else {
    toastError("Cannot cancel payment");
}
} catch (e) {
    console.error(e);
    toastError("Cannot cancel payment");
}
}

    checkoutForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
    const fd = new FormData(checkoutForm);

    const res = await fetch("/perfume-shop/checkout", {
    method: "POST",
    body: fd,
    headers: {
    "Accept": "application/json",
    "X-CSRF-TOKEN": csrfInput ? csrfInput.value : ""
}
});

    const json = await res.json();

    if (!res.ok || !json.ok) {
    throw new Error(json.message || "Checkout failed");
}

    const paymentMethod = String(json.paymentMethod || "CASH").toUpperCase();

    if (paymentMethod === "KHQR") {
    await openKhqrModal(
    json.orderId,
    json.total,
    json.khqrString,
    json.md5
    );
} else {
    showOrderSuccess(json.orderId);
}

} catch (err) {
    console.error(err);
    toastError(err.message || "Checkout failed");
} finally {
    setLoading(false);
}
});

    btnCancelPayment.addEventListener("click", () => cancelPayment(true));
    btnCloseKhqr.addEventListener("click", () => cancelPayment(true));
})();

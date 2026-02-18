function updateTopbarClock(){
    const now = new Date();

    const dateStr = now.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: '2-digit',
        year: 'numeric'
    });

    const timeStr = now.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });

    // mobile format: 11:35 AM + 2/18/2026
    const dateMobile = now.toLocaleDateString('en-US');
    const timeMobile = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

    const d = document.getElementById('topbarDate');
    const t = document.getElementById('topbarTime');
    if(d) d.textContent = dateStr;
    if(t) t.textContent = timeStr;

    const dm = document.getElementById('topbarDateMobile');
    const tm = document.getElementById('topbarTimeMobile');
    if(dm) dm.textContent = dateMobile;
    if(tm) tm.textContent = timeMobile;
}

updateTopbarClock();
setInterval(updateTopbarClock, 1000);
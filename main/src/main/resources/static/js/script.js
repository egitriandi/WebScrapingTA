function toggleTextBox() {
    const checkboxes = document.querySelectorAll('input[name="portal"]:checked');
    const dateContainer = document.getElementById("labelTahun");
    const fromDate = document.getElementById("fromDate");
    const toDate = document.getElementById("toDate");

    let onlySintaSelected = true;

    checkboxes.forEach(checkbox => {
        if (checkbox.value !== "2") {
            onlySintaSelected = false;
        }
    });

    if (onlySintaSelected) {
        // Jika hanya SINTA yang dipilih, sembunyikan input jarak tahun
        dateContainer.style.display = 'none';
        fromDate.disabled = true;
        toDate.disabled = true;
    } else {
        // Jika ada portal lain yang dipilih, tampilkan input jarak tahun
        dateContainer.style.display = 'block';
        fromDate.disabled = false;
        toDate.disabled = false;
    }
}

// Tambahkan event listener ke checkbox untuk memicu toggleTextBox saat diklik
document.querySelectorAll('input[name="portal"]').forEach(checkbox => {
    checkbox.addEventListener('change', toggleTextBox);
});

function validateForm() {
    var portals = document.querySelectorAll("input[name='portal']:checked");
    var title = document.getElementById("inputTitle").value.trim();
    var fromDate = document.getElementById("fromDate").value.trim();
    var toDate = document.getElementById("toDate").value.trim();

    if (portals.length === 0) {
        alert("Silakan pilih minimal satu portal sebelum mencari.");
        return false; // Mencegah form submit
    }

    if (title === "") {
        alert("Judul tidak boleh kosong.");
        return false;
    }

    // Cek apakah user memilih Google Scholar atau GARUDA tanpa mengisi tahun
    var portalValues = Array.from(portals).map(p => p.value);
    if (portalValues.includes("0") || portalValues.includes("1")) {
        if(fromDate === "" || toDate === ""){
            alert("Silakan isi jarak tahun untuk Google Scholar dan GARUDA.");
                    return false;
        }
        if(fromDate >= toDate){
                alert("Jarak tahun tidak valid. Periksa kembali.");
                return false;
            }
    }

    return true; // Jika valid, lanjutkan submit
}

function validateTitle() {
    let inputField = document.getElementById("inputTitle");
    let errorMessage = document.getElementById("error-message");
    let title = inputField.value.trim(); // Hilangkan spasi di awal & akhir

    console.log("Input Judul:", title); // Debugging, cek input sebelum validasi

    if (title === "") {
        errorMessage.textContent = "Judul tidak boleh kosong!";
        return;
    }

    if (!/^[a-zA-Z0-9 ]+$/.test(title)) {
        errorMessage.textContent = "Judul hanya boleh mengandung huruf, angka, dan spasi!";
        return;
    }

    if (title.length < 5) {
        errorMessage.textContent = "Judul harus minimal 5 karakter!";
        return;
    }
}

// Mencegah input karakter spesial saat diketik
document.getElementById("inputTitle").addEventListener("input", function () {
    this.value = this.value.replace(/[^a-zA-Z0-9 ]/g, "");
});

document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");

    form.addEventListener("submit", function (event) {
        const portalSelect = document.getElementById("portal").value;
        const fromDate = document.getElementById("fromDate").value.trim();
        const toDate = document.getElementById("toDate").value.trim();
        const yearRegex = /^\d{4}$/; // Format tahun: 4 digit angka
        const currentYear = new Date().getFullYear();

        // Cek apakah portal adalah Google Scholar atau GARUDA
        if (portalSelect === "0" || portalSelect === "1") {
            if (fromDate === "" || toDate === "") {
                if(portalSelect === "0"){
                    alert("Input tahun wajib diisi untuk Google Scholar.");
                }
                if(portalSelect === "1"){
                    alert("Input tahun wajib diisi untuk GARUDA.");
                }
                event.preventDefault(); // Mencegah form terkirim
            }

             // Validasi bahwa input tahun harus 4 digit numerik
            if (!yearRegex.test(fromDate) || !yearRegex.test(toDate)) {
                alert("Format tahun harus 4 digit angka (misal: 2023).");
                event.preventDefault();
                return;
            }

            // Validasi bahwa input tahun tidak melebihi tahun saat ini
            if (fromDate > currentYear || toDate > currentYear) {
                alert("Tahun tidak boleh melebihi tahun saat ini (" + currentYear + ").");
                event.preventDefault();
                return;
            }

            // Validasi bahwa tahun 'dari' tidak lebih besar dari tahun 'sampai'
            if (fromDate > toDate) {
                alert("Tahun 'Dari' tidak boleh lebih besar dari tahun 'Sampai'.");
                event.preventDefault();
                return;
            }

            if (fromDate == toDate) {
                alert("Tahun 'Dari' tidak boleh sama dengan dari tahun 'Sampai'.");
                event.preventDefault();
                return;
            }
        }
    });
});

document.querySelectorAll("input[name='portal']").forEach(checkbox => {
    checkbox.addEventListener("change", function() {
        let selectedPortals = [];
        document.querySelectorAll("input[name='portal']:checked").forEach(cb => {
            selectedPortals.push(cb.value);
        });

        if (selectedPortals.length === 0) {
            console.log("Pilih minimal satu portal edukasi sebelum mencari!");
            return;
        }

        console.log("Portal yang dipilih:", selectedPortals);
        // Kirim `selectedPortals` ke backend untuk memproses pencarian
    });
});

function populateYearOptions(selectId, startYear) {
    const currentYear = new Date().getFullYear();
    const select = document.getElementById(selectId);

    for (let year = currentYear; year >= startYear; year--) {
        let option = document.createElement("option");
        option.value = year;
        option.textContent = year;
        select.appendChild(option);
    }
}

function scrapeJournal(url) {
    let popup = window.open("", "viewJournal", "width=800,height=600");

        fetch('/viewJournal', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ url: url })
        })
        .then(response => response.text())
        .then(data => {
            popup.document.write(data);
        })
        .catch(error => console.error('Error:', error));
}

document.addEventListener("DOMContentLoaded", function() {
    if (!sessionStorage.getItem("modalOpened")) {
        document.getElementById("journalModal").style.display = "none";
    }
});

function openJournalPopup(url) {
    document.getElementById("journalModal").style.display = "block";
    document.getElementById("modal-body").innerHTML = "<p>Memuat...</p>";
    sessionStorage.setItem("modalOpened", "true"); // Simpan status modal telah dibuka

    fetch("/viewJournal?url=" + encodeURIComponent(url))
        .then(response => response.text())
        .then(html => {
            document.getElementById("modal-body").innerHTML = html;
        })
        .catch(error => {
            document.getElementById("modal-body").innerHTML = "<p>Gagal mengambil data.</p>";
            console.error("Error:", error);
        });
}

function closeModal() {
    document.getElementById("journalModal").style.display = "none";
    document.body.classList.remove("modal-open");
    sessionStorage.removeItem("modalOpened"); // Hapus status modal saat ditutup
}

// Mengisi dropdown dari tahun 1990 hingga tahun sekarang
populateYearOptions("fromDate", 2000);
populateYearOptions("toDate", 2000);
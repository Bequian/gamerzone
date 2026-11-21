// Script propio del detalle de evento (permitido por la CSP: script-src 'self').
// No usa innerHTML ni eval: solo textContent, así no puede introducir XSS.
document.addEventListener('DOMContentLoaded', () => {

    // ===== Copiar enlace del evento =====
    const btnCopiar = document.getElementById('btn-copiar');
    const enlace = document.getElementById('enlace-evento');
    const estado = document.getElementById('copiar-estado');

    if (btnCopiar && enlace) {
        btnCopiar.addEventListener('click', async () => {
            // En celulares se abre el menú nativo para compartir (si el navegador lo permite)
            if (navigator.share) {
                try {
                    await navigator.share({ title: document.title, url: enlace.value });
                    return;
                } catch (e) {
                    // El usuario canceló: se continúa con copiar al portapapeles
                }
            }
            try {
                await navigator.clipboard.writeText(enlace.value);
                estado.textContent = '¡Enlace copiado!';
            } catch (e) {
                enlace.select();
                estado.textContent = 'Selecciona el enlace y cópialo con Ctrl+C';
            }
        });
    }

    // ===== Validación del tamaño de la foto antes de subirla =====
    // (solo mejora la experiencia: el servidor vuelve a validar tamaño, tipo y contenido)
    const archivo = document.getElementById('archivo');
    const errorArchivo = document.getElementById('archivo-error');

    if (archivo) {
        const maximo = Number(archivo.dataset.maxBytes);
        archivo.form.addEventListener('submit', (evento) => {
            const seleccionado = archivo.files[0];
            errorArchivo.textContent = '';
            if (seleccionado && seleccionado.size > maximo) {
                evento.preventDefault();
                errorArchivo.textContent = 'La imagen supera el tamaño máximo de 2 MB';
            } else if (seleccionado && !['image/jpeg', 'image/png'].includes(seleccionado.type)) {
                evento.preventDefault();
                errorArchivo.textContent = 'Solo se permiten imágenes JPG o PNG';
            }
        });
    }
});


document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', event => {
            if (!window.confirm(form.dataset.confirm)) event.preventDefault();
        });
    });
    const path = window.location.pathname;
    document.querySelectorAll('.sidebar nav a').forEach(link => {
        const target = new URL(link.href).pathname;
        if (target === path || (target === '/products' && path.startsWith('/products/edit/'))) {
            link.setAttribute('aria-current', 'page');
        }
    });
});

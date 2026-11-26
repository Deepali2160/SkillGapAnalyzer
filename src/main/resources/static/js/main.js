// SkillGap Analyzer — frontend interactions (presentation layer only,
// does not touch any form action, field name, or backend endpoint).

document.addEventListener('DOMContentLoaded', function () {

    // ---- Mobile nav toggle ----
    var navToggle = document.querySelector('.nav-toggle');
    var navLinks = document.querySelector('.nav-links');
    if (navToggle && navLinks) {
        navToggle.addEventListener('click', function () {
            navLinks.classList.toggle('open');
        });
    }

    // ---- Fade-in-on-scroll ----
    var revealEls = document.querySelectorAll('.reveal');
    if (revealEls.length && 'IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });
        revealEls.forEach(function (el) { observer.observe(el); });
    } else {
        revealEls.forEach(function (el) { el.classList.add('is-visible'); });
    }

    // ---- Password visibility toggle ----
    document.querySelectorAll('.password-toggle').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = document.getElementById(btn.dataset.target);
            if (!input) return;
            var isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            btn.textContent = isHidden ? 'Hide' : 'Show';
        });
    });

    // ---- Number count-up (stats / KPI cards) ----
    document.querySelectorAll('[data-count-to]').forEach(function (el) {
        var target = parseFloat(el.getAttribute('data-count-to'));
        if (isNaN(target)) return;
        var suffix = el.getAttribute('data-suffix') || '';
        var duration = 900;
        var start = null;

        var runCount = function (ts) {
            if (!start) start = ts;
            var progress = Math.min((ts - start) / duration, 1);
            var value = Math.floor(progress * target);
            el.textContent = value + suffix;
            if (progress < 1) requestAnimationFrame(runCount);
            else el.textContent = target + suffix;
        };

        if ('IntersectionObserver' in window) {
            var countObserver = new IntersectionObserver(function (entries) {
                entries.forEach(function (entry) {
                    if (entry.isIntersecting) {
                        requestAnimationFrame(runCount);
                        countObserver.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.4 });
            countObserver.observe(el);
        } else {
            el.textContent = target + suffix;
        }
    });

    // ---- Drag & drop resume upload ----
    document.querySelectorAll('.dropzone').forEach(function (zone) {
        var input = zone.querySelector('input[type="file"]');
        var filenameEl = zone.querySelector('.dz-filename');
        if (!input) return;

        ['dragenter', 'dragover'].forEach(function (evt) {
            zone.addEventListener(evt, function (e) {
                e.preventDefault();
                zone.classList.add('drag-over');
            });
        });
        ['dragleave', 'drop'].forEach(function (evt) {
            zone.addEventListener(evt, function (e) {
                e.preventDefault();
                zone.classList.remove('drag-over');
            });
        });
        zone.addEventListener('drop', function (e) {
            if (e.dataTransfer.files.length) {
                input.files = e.dataTransfer.files;
                updateFilename();
            }
        });
        input.addEventListener('change', updateFilename);

        function updateFilename() {
            if (input.files.length && filenameEl) {
                filenameEl.textContent = 'Selected: ' + input.files[0].name;
                filenameEl.style.display = 'block';
            }
        }
    });

    // ---- Loading state on form submit (does not block/alter submission) ----
    document.querySelectorAll('form[data-loading-btn]').forEach(function (form) {
        form.addEventListener('submit', function () {
            var btn = form.querySelector('button[type="submit"]');
            if (btn) {
                btn.classList.add('is-loading');
                btn.setAttribute('disabled', 'disabled');
            }
        });
    });
});

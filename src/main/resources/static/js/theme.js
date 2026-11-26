(function () {
    'use strict';

    const storageKey = 'todo-theme';
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

    function getStoredTheme() {
        try {
            return localStorage.getItem(storageKey);
        } catch (error) {
            return null;
        }
    }

    function getPreferredTheme() {
        return getStoredTheme() || (mediaQuery.matches ? 'dark' : 'light');
    }

    function applyTheme(theme) {
        document.documentElement.dataset.theme = theme;
        const toggle = document.querySelector('[data-theme-toggle]');
        if (toggle) {
            toggle.setAttribute('aria-label', theme === 'dark' ? 'ライトモードに切り替え' : 'ダークモードに切り替え');
            toggle.setAttribute('title', theme === 'dark' ? 'ライトモード' : 'ダークモード');
        }
    }

    applyTheme(getPreferredTheme());

    document.addEventListener('DOMContentLoaded', function () {
        const toggle = document.querySelector('[data-theme-toggle]');
        if (!toggle) return;

        toggle.addEventListener('click', function () {
            const nextTheme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
            applyTheme(nextTheme);
            try {
                localStorage.setItem(storageKey, nextTheme);
            } catch (error) {
                // Storage is optional; the current page still changes theme.
            }
        });

        const onSystemThemeChange = function (event) {
            if (!getStoredTheme()) applyTheme(event.matches ? 'dark' : 'light');
        };
        if (mediaQuery.addEventListener) {
            mediaQuery.addEventListener('change', onSystemThemeChange);
        } else {
            mediaQuery.addListener(onSystemThemeChange);
        }
    });
})();

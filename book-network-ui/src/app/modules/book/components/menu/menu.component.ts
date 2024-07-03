import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'app-menu',
    templateUrl: './menu.component.html',
    styleUrl: './menu.component.scss'
})
export class MenuComponent implements OnInit {

    ngOnInit(): void {
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            let attr = link.getAttribute('href') || '';
            if (window.location.href.endsWith(attr)) {
                link.classList.add('active');
            }
            link.addEventListener('click', () => {
                navLinks.forEach(link => {link.classList.remove('active');})
                link.classList.add('active');
            })
        })
    }

    logout() {
        localStorage.removeItem('book-social-network-token');
        window.location.reload();
    }
}

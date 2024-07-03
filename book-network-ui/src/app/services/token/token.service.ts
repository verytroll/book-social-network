import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
    providedIn: 'root'
})
export class TokenService {

    set token(token: string) {
        localStorage.setItem('book-social-network-token', token);
    }

    get token() {
        return localStorage.getItem('book-social-network-token') as string;
    }

    isTokenNotValid() {
        return !this.isTokenValid();
    }

    isTokenValid() {
        let result = false;

        const token = this.token;
        if(token) {
            const jwtHelper = new JwtHelperService();
            const isTokenExpired = jwtHelper.isTokenExpired(token);
            if(isTokenExpired) {
                localStorage.removeItem('book-social-network-token');
            } else {
                result = true;
            }
        }

        return result;
    }
}

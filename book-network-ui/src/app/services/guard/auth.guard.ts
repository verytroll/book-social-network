import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../token/token.service';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
    let result = true;
    const tokenService = inject(TokenService);
    const router = inject(Router);
    if(tokenService.isTokenNotValid()) {
        router.navigate(['login']);
        result = false;
    }
    return result;
};

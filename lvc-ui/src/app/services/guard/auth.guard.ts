import {CanActivateFn, Router} from '@angular/router';
import {TokenService} from '../token/token.service';
import {inject} from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const tokenService = inject(TokenService); //have to inject because constructors don't work here
  const router = inject(Router);
  if (tokenService.isTokenNotValid()){
    router.navigate(['login']);
    return false;
  }
  return true;
};

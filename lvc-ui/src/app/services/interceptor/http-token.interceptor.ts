import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../token/token.service';

export const HttpTokenInterceptor: HttpInterceptorFn = (req, next) =>  {

  const tokenService = inject(TokenService);
  const token = tokenService.token;

  if (token){
    const authRequest = req.clone({
      setHeaders:{
        Authorization: `Bearer ${token}`
      }
    });
    return next(authRequest);
  }
  return next(req);

}







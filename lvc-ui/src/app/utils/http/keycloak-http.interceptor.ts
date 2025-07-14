import { HttpInterceptorFn } from '@angular/common/http';

export const keycloakHttpInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req);
};

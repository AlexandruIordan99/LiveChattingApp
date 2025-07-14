import {ApplicationConfig, inject, provideAppInitializer, provideZoneChangeDetection} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {provideHttpClient} from '@angular/common/http';
import {KeycloakService} from './utils/keycloak/keycloak.service';

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    provideAppInitializer(() => {
      const initFn = ((key: KeycloakService) =>{
        return () => key.init();
      }) (inject(KeycloakService));
      return initFn();
    })
  ]
};

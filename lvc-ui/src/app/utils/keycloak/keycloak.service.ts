import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private _keycloak: Keycloak | undefined;

  constructor() { }

  get keycloak(){
    if(!this._keycloak){
      this._keycloak = new Keycloak({
        clientId: "LiveChattingApp",
        url: "https://localhost:8090",
        realm: "LiveChattingApp"
      })
    }
    return this._keycloak;
  }
}

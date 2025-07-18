/* tslint:disable */
/* eslint-disable */
/* Code generated by ng-openapi-gen DO NOT EDIT. */

import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { addParticipant } from '../fn/chat/add-participant';
import { AddParticipant$Params } from '../fn/chat/add-participant';
import { createDirectChat } from '../fn/chat/create-direct-chat';
import { CreateDirectChat$Params } from '../fn/chat/create-direct-chat';
import { createGroupChat } from '../fn/chat/create-group-chat';
import { CreateGroupChat$Params } from '../fn/chat/create-group-chat';
import { getChatParticipants } from '../fn/chat/get-chat-participants';
import { GetChatParticipants$Params } from '../fn/chat/get-chat-participants';
import { leaveChat } from '../fn/chat/leave-chat';
import { LeaveChat$Params } from '../fn/chat/leave-chat';
import { removeParticipant } from '../fn/chat/remove-participant';
import { RemoveParticipant$Params } from '../fn/chat/remove-participant';
import { User } from '../models/user';

@Injectable({ providedIn: 'root' })
export class ChatService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `getChatParticipants()` */
  static readonly GetChatParticipantsPath = '/api/v1/chats/{chatId}/participants';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getChatParticipants()` instead.
   *
   * This method doesn't expect any request body.
   */
  getChatParticipants$Response(params: GetChatParticipants$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<User>>> {
    return getChatParticipants(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `getChatParticipants$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getChatParticipants(params: GetChatParticipants$Params, context?: HttpContext): Observable<Array<User>> {
    return this.getChatParticipants$Response(params, context).pipe(
      map((r: StrictHttpResponse<Array<User>>): Array<User> => r.body)
    );
  }

  /** Path part for operation `addParticipant()` */
  static readonly AddParticipantPath = '/api/v1/chats/{chatId}/participants';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `addParticipant()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  addParticipant$Response(params: AddParticipant$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return addParticipant(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `addParticipant$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  addParticipant(params: AddParticipant$Params, context?: HttpContext): Observable<void> {
    return this.addParticipant$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  /** Path part for operation `leaveChat()` */
  static readonly LeaveChatPath = '/api/v1/chats/{chatId}/leave';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `leaveChat()` instead.
   *
   * This method doesn't expect any request body.
   */
  leaveChat$Response(params: LeaveChat$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return leaveChat(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `leaveChat$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  leaveChat(params: LeaveChat$Params, context?: HttpContext): Observable<void> {
    return this.leaveChat$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  /** Path part for operation `createGroupChat()` */
  static readonly CreateGroupChatPath = '/api/v1/chats/group';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `createGroupChat()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  createGroupChat$Response(params: CreateGroupChat$Params, context?: HttpContext): Observable<StrictHttpResponse<string>> {
    return createGroupChat(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `createGroupChat$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  createGroupChat(params: CreateGroupChat$Params, context?: HttpContext): Observable<string> {
    return this.createGroupChat$Response(params, context).pipe(
      map((r: StrictHttpResponse<string>): string => r.body)
    );
  }

  /** Path part for operation `createDirectChat()` */
  static readonly CreateDirectChatPath = '/api/v1/chats/direct';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `createDirectChat()` instead.
   *
   * This method doesn't expect any request body.
   */
  createDirectChat$Response(params: CreateDirectChat$Params, context?: HttpContext): Observable<StrictHttpResponse<string>> {
    return createDirectChat(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `createDirectChat$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  createDirectChat(params: CreateDirectChat$Params, context?: HttpContext): Observable<string> {
    return this.createDirectChat$Response(params, context).pipe(
      map((r: StrictHttpResponse<string>): string => r.body)
    );
  }

  /** Path part for operation `removeParticipant()` */
  static readonly RemoveParticipantPath = '/api/v1/chats/{chatId}/participants/{userId}';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `removeParticipant()` instead.
   *
   * This method doesn't expect any request body.
   */
  removeParticipant$Response(params: RemoveParticipant$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return removeParticipant(this.http, this.rootUrl, params, context);
  }

  /**
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `removeParticipant$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  removeParticipant(params: RemoveParticipant$Params, context?: HttpContext): Observable<void> {
    return this.removeParticipant$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

}

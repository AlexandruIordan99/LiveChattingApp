/* tslint:disable */
/* eslint-disable */
/* Code generated by ng-openapi-gen DO NOT EDIT. */

export interface MessageRequest {
  chatId?: string;
  content?: string;
  receiverId?: string;
  replyToId?: string;
  senderId?: string;
  type?: 'JOIN' | 'LEAVE' | 'TEXT' | 'IMAGE' | 'AUDIO' | 'VIDEO';
}

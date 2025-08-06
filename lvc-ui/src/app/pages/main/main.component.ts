import {AfterViewChecked, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ChatListComponent} from '../../components/chat-list/chat-list.component';
import {ChatService} from '../../services/services/chat.service';
import {ChatResponse} from '../../services/models/chat-response';
import {MessageResponse} from '../../services/models/message-response';
import {DatePipe, NgOptimizedImage} from '@angular/common';
import {MessageService} from '../../services/services/message.service';
import {FormsModule} from '@angular/forms';
import { PickerModule } from '@ctrl/ngx-emoji-mart';
import {UserService} from '../../services/services/user.service';

@Component({
  selector: 'app-main',
  imports: [
    ChatListComponent,
    FormsModule,
    DatePipe,
    FormsModule,
    PickerModule
  ],
  templateUrl: './main.component.html',
  standalone: true,
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit, OnDestroy, AfterViewChecked {

  selectedChat: ChatResponse = {};
  chats: Array<ChatResponse> = [];
  chatMessages: Array<MessageResponse> = [];
  socketClient: any = null;
  messageContent: string = '';
  showEmojis = false;
  @ViewChild('scrollableDiv') scrollableDiv!: ElementRef<HTMLDivElement>;
  private notificationSubscription: any;

  constructor(
    private chatService: ChatService,
    private messageService: MessageService,
    private userService: UserService,
  ) {
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    if (this.socketClient !== null) {
      this.socketClient.disconnect();
      this.notificationSubscription.unsubscribe();
      this.socketClient = null;
    }
  }

  ngOnInit(): void {
    this.getAllChats();
  }


  private getAllChats() {
    this.chatService.getChatsByReceiver()
      .subscribe({
        next: (res) => {
          this.chats = res;
        }
      });
  }


  private scrollToBottom() {
    if (this.scrollableDiv) {
      const div = this.scrollableDiv.nativeElement;
      div.scrollTop = div.scrollHeight;
    }
  }

  private extractFileFromTarget(target: EventTarget | null): File | null {
    const htmlInputTarget = target as HTMLInputElement;
    if (target === null || htmlInputTarget.files === null) {
      return null;
    }
    return htmlInputTarget.files[0];
  }

  chatSelected(chatResponse: ChatResponse){
    this.selectedChat = chatResponse;
    this.getAllChatMessages(chatResponse.id as number);
    // this.setMessagesToSeen();
    this.selectedChat.unreadCount = 0;
  }

  private getAllChatMessages(chatId: number) {
    this.messageService.getChatMessages({chatId: chatId
    }).subscribe({
      next: (messages) => {
        this.chatMessages = messages;
      }
    });
  }


  uploadMedia(target: EventTarget | null) {

  }

  onSelectEmojis(event: any) {
    this.messageContent += event.emoji.native;
    this.showEmojis = false;
  }

  keyDown($event: KeyboardEvent) {

  }

  onClick() {

  }

  sendMessage() {

  }

  logout() {

  }

  userProfile() {

  }

  isSelfMessage(message: MessageResponse): boolean {
    return false;
  }

}

import {Component, input, InputSignal} from '@angular/core';
import {ChatResponse} from '../../services/models/chat-response';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.component.html',
  standalone: true,
  styleUrls: ['./chat-list.component.scss']
})
export class ChatListComponent {


  chats: InputSignal<ChatResponse[]> = input<ChatResponse[]>([]);
  searchContact = false;

}

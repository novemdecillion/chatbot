import { Component } from '@angular/core';
import { Observable, OperatorFunction } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { SessionChatMessageFragment, MessagesGQL, MessagesSubscription } from 'src/generated/graphql';
import { SubscriptionResult } from 'apollo-angular';

interface MessageRecord {
  message: string;
  isSend: boolean;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  messages: MessageRecord[] = [];
  $messages: Observable<MessageRecord[]>

  constructor(messagesGql: MessagesGQL) {
    this.$messages = messagesGql.subscribe()
      .pipe(
        map(res => res.data?.message),
        filter(message => message !== null) as OperatorFunction<SessionChatMessageFragment | undefined, SessionChatMessageFragment>,
        map(message => {
          this.messages.push({
              message: message.nodes.join(),
              isSend: message.isSend
            });
          return this.messages;
        })
      );
  }



}

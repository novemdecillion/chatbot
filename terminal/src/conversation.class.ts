export type MessageType = 'SCENARIO' | 'TALK';

export interface MessageNode {
  text: string;
  link?: string;
}

export interface ChatMessage {
  type?: MessageType;
  nodes?: MessageNode[];
  isSend?: boolean;
}

export interface SendMessage {
  type: MessageType;
  text: string;
  link?: string;
}

export class Conversation {
  messages: ChatMessage[] = [];
  socket: null | WebSocket = null;

  constructor(private serverUrl, private onMessage: (message: ChatMessage) => void = ()=>{}) {}

  connect(message?: string) {
    if (this.socket !== null) {
      this.close();
    }
    this.socket = new WebSocket(this.serverUrl + ((undefined !== message) ? '?reconnect=true' : ''));
    this.socket.onopen = () => {
      if (undefined !== message) {
        this.socket.send(message);
      }
    };
    this.socket.onerror = (event: Event) => {
      console.log(event);
      let message = { nodes: [ { text: 'サーバとの接続に失敗しました。' } ]};
      this.messages.push(message);
      this.onMessage(message);
    };
    this.socket.onclose = () => {
      this.socket = null;
    }
    this.socket.onmessage = (event) => {
      if(!event.data) {
        return 
      }
      let message = JSON.parse(event.data);
      this.messages = this.messages.concat(message);
      this.onMessage(message)
    };
  }

  close() {
    if (this.socket !== null) {
      this.socket.close();
      this.socket = null
    }
  }

  send(message: string) {
    if (this.socket) {
      this.socket.send(message);
    } else {
      this.connect(message)
    }
  }

  clear() {
    this.messages = [];
  }
}
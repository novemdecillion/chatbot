<svelte:options tag="chat-terminal"/>

<script lang="ts">
  import { onMount, tick } from 'svelte';
  import { Conversation } from './conversation.class';
  import type { ChatMessage, MessageNode, SendMessage } from './conversation.class';

  export let title = 'チャットボット'
  export let opened: string = 'false';
  export let serverurl: string;
  export let openclass: string = 'open';
  export let closeclass: string = 'close';
  export let cantalk: string = 'true';

  let conversation = new Conversation(serverurl, () => {
    messages = [...conversation.messages];
    scrollToBottom();
  });

  let isOpen = false;
  let messages: ChatMessage[];
  $: messages = [];

  let sendText: string;
  let hostElement: Element;
  let body: null | Element;

  onMount(async () => {
    isOpen = (opened == 'true');

    hostElement =  (body.parentNode as ShadowRoot).host;
    modifyCustomElementClass();
    if (isOpen) {
      conversation.connect();
    }
  });

  function modifyCustomElementClass() {
    if (hostElement) {
      hostElement.classList.remove(isOpen ? closeclass : openclass);
      hostElement.classList.add(isOpen ? openclass : closeclass);
    }
  }

  function sendMessage(message: SendMessage) {
    conversation.send(JSON.stringify(message));
  }

  function onToggleOpen() {
    isOpen = !isOpen;
    modifyCustomElementClass();
    if (isOpen) {
      conversation.connect();
    } else {
      conversation.close();
      conversation.clear();
    }
  }

  function onSend() {
    sendMessage({
      type: 'TALK',
      text: sendText
    });
    sendText = '';
  }

  function onKeyupEvent(event: KeyboardEvent): void {
    if (event.ctrlKey && (event.code === 'Enter')) {
      onSend();
    }
  }

  function onClickLink(message: ChatMessage, node: MessageNode) {
    sendMessage({
      type: message.type,
      text: node.text,
      link: node.link
    });
  }

  async function scrollToBottom() {
    await tick();
    body.scrollTop = body.scrollHeight;
  };  
</script>

<style lang="scss">
  [part="body"] {
    p {
      margin-block-start: 0px;
      margin-block-end: 0px;
    }
  }
</style>

<div part="header">
  <div part="title">{title}</div>
  <button on:click={onToggleOpen} part="toggle">
  </button>
</div>

<div part="body" bind:this={body}>

  {#each messages as msg}
    <div part={msg.isSend ? "receive-message" : "send-message"}> 
      {#each msg.nodes as msgNode}
        {#if msgNode.link}
          <p><a href={'#'} on:click={() => onClickLink(msg, msgNode)}>{@html msgNode.text}</a></p>
        {:else}
          {@html msgNode.text}
        {/if}
      {/each}
    </div>
  {/each}
</div>

{#if cantalk == 'true'}
<div part="footer" >
  <input type="text" part="query-text"
    placeholder="こちらに入力してください。"
    bind:value={sendText}
    on:keyup={onKeyupEvent}>

  <button type="button" part="query-button" on:click={onSend}>送信</button>
</div>
{/if}

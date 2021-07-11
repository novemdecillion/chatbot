import { gql } from 'apollo-angular';
import { Injectable } from '@angular/core';
import * as Apollo from 'apollo-angular';
export type Maybe<T> = T | null;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};



export type MessageNode = {
  __typename?: 'MessageNode';
  text: Scalars['String'];
  link?: Maybe<Scalars['String']>;
};

export type Query = {
  __typename?: 'Query';
  sessions?: Maybe<Array<Session>>;
};

export type Session = {
  __typename?: 'Session';
  sessionId: Scalars['ID'];
};

export type SessionChatMessage = {
  __typename?: 'SessionChatMessage';
  sessionId: Scalars['ID'];
  nodes: Array<MessageNode>;
  isSend: Scalars['Boolean'];
};

export type Subscription = {
  __typename?: 'Subscription';
  message: SessionChatMessage;
};

export type MessageNodeFragment = (
  { __typename?: 'MessageNode' }
  & Pick<MessageNode, 'text' | 'link'>
);

export type SessionChatMessageFragment = (
  { __typename?: 'SessionChatMessage' }
  & Pick<SessionChatMessage, 'sessionId' | 'isSend'>
  & { nodes: Array<(
    { __typename?: 'MessageNode' }
    & MessageNodeFragment
  )> }
);

export type MessagesSubscriptionVariables = Exact<{ [key: string]: never; }>;


export type MessagesSubscription = (
  { __typename?: 'Subscription' }
  & { message: (
    { __typename?: 'SessionChatMessage' }
    & SessionChatMessageFragment
  ) }
);

export const MessageNodeFragmentDoc = gql`
    fragment messageNode on MessageNode {
  text
  link
}
    `;
export const SessionChatMessageFragmentDoc = gql`
    fragment sessionChatMessage on SessionChatMessage {
  sessionId
  nodes {
    ...messageNode
  }
  isSend
}
    ${MessageNodeFragmentDoc}`;
export const MessagesDocument = gql`
    subscription messages {
  message {
    ...sessionChatMessage
  }
}
    ${SessionChatMessageFragmentDoc}`;

  @Injectable({
    providedIn: 'root'
  })
  export class MessagesGQL extends Apollo.Subscription<MessagesSubscription, MessagesSubscriptionVariables> {
    document = MessagesDocument;
    
    constructor(apollo: Apollo.Apollo) {
      super(apollo);
    }
  }
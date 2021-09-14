import {Component, OnDestroy, OnInit} from '@angular/core';
import {MessageService} from "../../../services/components/message.service";
import {Constants} from "../../../types/constants";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss']
})
export class MessagesComponent implements OnInit, OnDestroy{

  public pending_messages:any[]=[];
  public messages:any[]=[];
  private messagesSubscription: Subscription;

  constructor(private messageService: MessageService) {}

  ngOnInit(): void {
    this.messagesSubscription=this.messageService.messagesSub.subscribe((message)=>this.processMessage(message));
  }

  private processMessage(message: any) {
    console.log("Processing new message");
    //Choose the queue
    let queue=this.messages.length<Constants.MESSAGE_MAX_NUM?
      this.messages:
      this.pending_messages;
    //Send message to the queue
    queue.push(message);
  }

  onMessageFinished(message) {
    console.log("Finished: ", message);

    MessagesComponent.removeMessageFromQueue(this.messages,message);

    //Promote pending to messages queue
    while(this.pending_messages.length>0 && this.messages.length<Constants.MESSAGE_MAX_NUM){
      let nextMessage=MessagesComponent.popFirstMessage(this.pending_messages);
      this.messages.push(nextMessage);
    }
  }

  private static removeMessageFromQueue(queue,message) {
    let pos = queue.indexOf(message);
    if (pos >= 0) queue.splice(pos, 1);
  }

  private static popFirstMessage(queue){
    let nextMessage=queue[0];
    queue.splice(0,1);
    return nextMessage;
  }

  testEvent() {
    this.messageService.addMessage(
      {
        text: "This is a sample long text to be written This is a sample long text to be written This is a sample long text to be written This is a sample long text to be written Random test: "+Math.round(Math.random()*100),
        link: "dirs"
      },
    );
  }

  ngOnDestroy() {
    this.messagesSubscription && this.messagesSubscription.unsubscribe();
  }
}

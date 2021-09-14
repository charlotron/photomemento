import {Injectable} from '@angular/core';
import {Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private _messagesSub: Subject<string> = new Subject<string>();

  constructor() {}

  /**
   * A message can be an object with the following fields:
   * {
   *   text:"some text"
   *   link:"some link"
   * }
   * or just a text "some message"
   * @param message
   */
  addMessage(message){
    this._messagesSub.next(message);
  }

  get messagesSub(): Subject<any> {
    return this._messagesSub;
  }
}

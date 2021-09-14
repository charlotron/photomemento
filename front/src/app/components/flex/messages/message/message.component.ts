import {Component, Input, OnInit, Output} from '@angular/core';
import {Subject} from "rxjs";
import {Constants} from "../../../../types/constants";

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss']
})
export class MessageComponent implements OnInit {

  @Input() message: any;
  @Input() index: number;
  @Output() finished: Subject<any> = new Subject<any>();
  visible:boolean;
  starting: boolean;

  constructor() {}

  ngOnInit(): void {
    this.showAndHideModal();
  }

  showModalStart(){
    this.starting=true;
  }

  showModal() {
    this.visible=true;
  }

  hideModal() {
    this.visible=false;
    this.finished.next(this.message);
  }

  autoShowModal() {
    setTimeout(() => {
      this.showModalStart();
    }, 400);
    setTimeout(() => {
      this.showModal();
    }, Constants.MESSAGE_SHOW_MODAL_TIME);
  }

  autoHideModal() {
    setTimeout(() => {
      this.hideModal();
    }, Constants.MESSAGE_HIDE_MODAL_TIME);
  }

  showAndHideModal() {
    this.autoShowModal();
    setTimeout(() => {
      this.autoHideModal();
    }, Constants.MESSAGE_SHOW_MODAL_TIME);
  }
}

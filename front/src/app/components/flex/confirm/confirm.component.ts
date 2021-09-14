import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-confirm',
  templateUrl: './confirm.component.html',
  styleUrls: ['./confirm.component.scss']
})
export class ConfirmComponent implements AfterViewInit {

  @ViewChild('frame')
  public frame;

  @Input('text')
  private _text: string="¡¡¡¡¡¡Text was not found!!!!!";
  @Input('okText')
  private _okText: string="Ok";
  @Input('cancelText')
  private _cancelText: string="Cancel";

  @Output()
  private ok: EventEmitter<any> = new EventEmitter<any>();
  @Output()
  private cancel: EventEmitter<any> = new EventEmitter<any>();

  constructor() {} //NOSONAR

  ngAfterViewInit(): void {
    console.log("frame: ", this.frame);
  }

  get text(): string {
    return this._text;
  }

  get okText(): string {
    return this._okText;
  }

  get cancelText(): string {
    return this._cancelText;
  }

  onOk() {
    this.ok.emit();
    this.close();
  }

  onCancel(){
    this.cancel.emit();
    this.close();
  }

  close() {
    this.frame.hide();
  }
}

import { Injectable } from '@angular/core';
import {Subject} from "rxjs";

/**
 * Events inserted in app.component.ts
 */
@Injectable({
  providedIn: 'root'
})
export class KeyEventsService {

  private onKeyPressObs:Subject<KeyboardEvent>=new Subject<KeyboardEvent>();

  constructor() { }

  get keyPress():Subject<KeyboardEvent>{
    return this.onKeyPressObs;
  }
}

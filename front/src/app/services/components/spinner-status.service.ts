import {Injectable} from '@angular/core';
import {Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SpinnerStatusService {

  constructor() {}

  public newSpinnerStatus(): any {
    let spinnerStatus: any = {
      waitingEvents: 0,
      subject: new Subject()
    };
    spinnerStatus.waitEventStarted = () => this.waitEventStarted(spinnerStatus);
    spinnerStatus.waitEventFinished = () => this.waitEventFinished(spinnerStatus);
    return spinnerStatus;
  }

  private waitEventStarted(spinnerStatus) {
    spinnerStatus.waitingEvents++;
  }

  private waitEventFinished(spinnerStatus) {
    if(spinnerStatus.waitingEvents==0) return;
    spinnerStatus.waitingEvents--;
    if (!spinnerStatus.waitingEvents)
      spinnerStatus.subject.next(true);
  }

}

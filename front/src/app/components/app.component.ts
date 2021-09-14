import {Component, HostListener, OnInit} from '@angular/core';
import {KeyEventsService} from "../services/api/key-events.service";
import {EnvVarsService} from "../services/env-vars.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent{
  //TODO: Update title based on section change
  title = 'front';

  constructor(private keyEventsService:KeyEventsService, private envVarsService:EnvVarsService) {}


  @HostListener('window:keyup', ['$event'])
  keyEvent(event: KeyboardEvent) {
    this.keyEventsService.keyPress.next(event);
  }
}

import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
// Add these three lines above the constructor entry.
  @Input() showBackButton: boolean;
  @Input() currentTitle: string;
  @Input() showHistoryNav: boolean;

  constructor() { }

  ngOnInit() {
  }

}

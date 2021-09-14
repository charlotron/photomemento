import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-sublink',
  templateUrl: './sublink.component.html',
  styleUrls: ['./sublink.component.scss']
})
export class SublinkComponent implements OnInit {
  @Input() subsections: any;
  @Input() depth: number;
  @Input("navbar") navbar;

  constructor() { }

  ngOnInit(): void {
  }

}

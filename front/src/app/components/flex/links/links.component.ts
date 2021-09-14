import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {SectionsService} from "../../../services/sections.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-links',
  templateUrl: './links.component.html',
  styleUrls: ['./links.component.scss']
})
export class LinksComponent implements OnInit {

  @Input("navbar") navbar;

  constructor(public sectionsServiceService:SectionsService, private route:ActivatedRoute) { }

  ngOnInit(): void {
  }
}

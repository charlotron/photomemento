import {Component} from '@angular/core';
import {SectionsService} from "../../../services/sections.service";
import {BreadCrumbsDataService} from "../../../services/components/bread-crumbs-data.service";

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.scss']
})
export class BreadcrumbsComponent {

  constructor(public sectionsServiceService: SectionsService, public breadCrumbsDataServiceService: BreadCrumbsDataService) {}

  disableClickIfInvalidPath(path: string, event: Event) {
    if(!path){
      event.stopPropagation();
      event.preventDefault();
    }
  }
}

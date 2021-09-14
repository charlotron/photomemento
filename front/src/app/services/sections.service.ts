import {Injectable} from '@angular/core';
import {ActivatedRoute, Route, Router, Routes, RoutesRecognized} from "@angular/router";
import {BreadCrumbsDataService} from "./components/bread-crumbs-data.service";

@Injectable({
  providedIn: 'root'
})
export class SectionsService {
  private readonly _sections: Routes;
  private _activeSection: Route;
  private readonly _home: Route;

  constructor(private route: ActivatedRoute, private router: Router, private breadCrumbsDataService:BreadCrumbsDataService) {
    this._sections = router.config;
    this._home=this._sections.find((config:Route)=>config.path.length==0);
    // Listen to route changes
    router.events.subscribe(event => {
      if (event instanceof RoutesRecognized) {
        breadCrumbsDataService.reset();
        this._activeSection = event.state.root.firstChild.routeConfig;
        if(this._activeSection!=this._home){
          breadCrumbsDataService.registerIcon("/", undefined, this._home.data.icon);
          breadCrumbsDataService.registerText(this._activeSection.path,undefined, this._activeSection.data.aliases.short);
        }
      }
    });
  }

  clearSubsections(section){
    section.data.subsections=undefined;
  }

  addSectionSubsection(section, path: any[] | string, queryParams:any, text: string, isActive?:boolean) {
    let subsections:any[];
    if(section.data){
      if(!section.data.subsections){
        subsections=[];
        section.data.subsections=subsections;
      }
      else
        subsections=section.data.subsections;
    }
    else{
      if(section.subsections)
        subsections=section.subsections;
      else {
        subsections=[];
        section.subsections=subsections;
      }
    }

    let subsection:any={path, queryParams, text};
    if(isActive) subsection.active=true;
    subsections.push(subsection);
    return subsection;
  }

  get sections(): Routes {
    return this._sections;
  }

  get activeSection(): Route {
    return this._activeSection;
  }

  get home(): Route {
    return this._home;
  }

  isHome() {
    return this._home==this._activeSection;
  }
}

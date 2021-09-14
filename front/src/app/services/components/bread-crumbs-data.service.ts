import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BreadCrumbsDataService {
  private _breadcrumbs: any[];

  constructor() {
    this._breadcrumbs = [];
  }

  registerIcon(path: any[] | string, queryParams: any, icon: string) {
    this._breadcrumbs.push({path, icon});
  }

  registerText(path: any[] | string, queryParams: any, text: string) {
    this._breadcrumbs.push({path, queryParams, text});
  }

  get breadcrumbs(): any[] {
    return this._breadcrumbs;
  }

  reset() {
    this._breadcrumbs=[];
  }
}

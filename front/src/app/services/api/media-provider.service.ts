import {AfterViewInit, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {RestRequestService} from "./rest-request.service";
import {Subject} from "rxjs";
import {SectionsService} from "../sections.service";

@Injectable({
  providedIn: 'root'
})
export class MediaProviderService {
  private _mediaPage: number;
  private _media: any;
  private _mediaTotalResults: any;
  private _mediaHasMoreResults: boolean;
  private _mediaInitSub: Subject<any> = new Subject<any>();
  private _mediaChangedSub: Subject<any> = new Subject<any>();
  private _searchTerm: string;

  constructor(
    private http: HttpClient,
    private restRequestService: RestRequestService,
    private sectionSvc: SectionsService) {
  }

  init(searchTerm:string): void {
    this._searchTerm=searchTerm;
    this._mediaInitSub.next(true);
  }

  async checkMedia(page?: number) {
    this._mediaPage = page || 0;
    let content = null;
    try {
      let section = this.sectionSvc.activeSection;
      let res: any = this._searchTerm?
          await this.restRequestService.searchMediaGlobal(this._searchTerm,page):
          await this.restRequestService.getMediaGlobal(page);
      content = res && res.content;
      if (section != this.sectionSvc.activeSection) return;
      if (page)
        this._media.push(...content);
      else
        this._media = content;
      this._mediaTotalResults = res.totalElements;
      this._mediaHasMoreResults = !res.last;
    } catch (e) {
      console.error("Error requesting media, due to: ", e);
    }
    this._mediaChangedSub.next(content); //Emit next group of results
  }

  async nextMediaPage(){
    if(!this._mediaHasMoreResults) return;
    await this.checkMedia(this._mediaPage+1);
  }

  get mediaInitSub(): Subject<any> {
    return this._mediaInitSub;
  }

  get mediaChangedSub(): Subject<any> {
    return this._mediaChangedSub;
  }

  public hasMoreResults(): boolean {
    return this._mediaHasMoreResults;
  }

  clear() {
    this._media = undefined;
    this._mediaPage = undefined;
    this._mediaTotalResults = undefined;
    this._mediaHasMoreResults = undefined
    this._searchTerm=undefined;
  }
}

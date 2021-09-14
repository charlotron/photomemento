import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {RestRequestService} from "./rest-request.service";
import {Subject} from "rxjs";
import {MessageService} from "../components/message.service";
import {SectionsService} from "../sections.service";

@Injectable({
  providedIn: 'root'
})
export class DirectoryProviderService {
  private _rootDir: any;
  private _activeDirChilds: any[];
  private _activeDirBrothers: any[];
  private _activeDir: any;
  private _activeDirId: string;
  private _activeDirMedia: any[];
  private _activeDirChangedSub: Subject<string> = new Subject<string>();
  private _activeDirMediaChangedSub: Subject<any> = new Subject<any>();
  private _activeDirMediaPage: number;
  private _activeDirMediaTotalResults: any;
  private _activeDirMediaHasMoreResults: boolean;

  constructor(
    private http: HttpClient,
    private restRequestService: RestRequestService,
    private messageService: MessageService,
    private sectionSvc: SectionsService) {
  }

  public async activeDirChanged(activeDirId?: string) {
    try{
      await this.checkRootDir();
      await this.checkActiveDir(activeDirId);
      await this.checkChilds();
      await this.checkBrothers();
      await this.checkMedia();
    }catch (e){
      console.error("Error requesting data for activeDir: ",activeDirId,", due to: ",e);
      this._activeDirMediaChangedSub.next(null); //Emit error
      throw e;
    }
  }

  async checkActiveDir(id?: string) {
    if (!this._rootDir) return;

    this._activeDirId = id || this.rootDir.id;
    this.clearActiveDir();

    this._activeDir = this._activeDirId === this.rootDir.id ?
      this._rootDir :
      await this.restRequestService.getDirectoryById(id);

    this._activeDirChangedSub.next(this._activeDirId);
  }

  clearActiveDir() {
    this._activeDirChilds = undefined;
    this._activeDirBrothers = undefined;
    this._activeDirMedia = undefined;
    this._activeDirMediaPage = undefined;
    this._activeDirMediaTotalResults = undefined;
    this._activeDirMediaHasMoreResults = undefined;
  }

  public activeIsRoot() {
    return this.rootDir && this._activeDirId === this.rootDir.id;
  }

  async checkChilds() {
    if (!this._activeDirId) return;
    let res: any = await this.restRequestService.getDirectoryChildsById(this._activeDirId);
    this._activeDirChilds = res && res.content;
  }

  async checkBrothers() {
    if (!this._activeDirId) return;
    let res: any = await this.restRequestService.getDirectoryChildsById(this._activeDir.parentHash);
    this._activeDirBrothers = res && res.content;
  }

  async checkRootDir() {
    if (this._rootDir == null) {
      let res: any = await this.restRequestService.getRootDirectory();
      this._rootDir = res && res.content[0];
    }
  }

  async checkMedia(page?: number) {
    if (!this._activeDirId) return;
    this._activeDirMediaPage = page || 0;
    let content = null;
    try {
      let id=this._activeDirId;
      let section = this.sectionSvc.activeSection;
      let res: any = await this.restRequestService.getDirectoryMediaById(id, page);
      content = res && res.content;
      if(this._activeDirId!=id || section!=this.sectionSvc.activeSection) return;
      if (page)
        this._activeDirMedia.push(...content);
      else
        this._activeDirMedia = content;
      this._activeDirMediaTotalResults = res.totalElements;
      this._activeDirMediaHasMoreResults = !res.last;
    } catch (e) {
      console.error("Error requesting media for directory: ", this._activeDir, ", error: ", e);
      throw e;
    }
    this._activeDirMediaChangedSub.next(content); //Emit next group of results
  }

  async nextMediaPage() {
    if (!this._activeDirMediaHasMoreResults) return;
    await this.checkMedia(this._activeDirMediaPage + 1);
  }

  async reprocessActiveDir(){
    await this.processResponseAsMessage(await this.restRequestService.reprocessFiles(this._activeDirId));
  }

  private async processResponseAsMessage(res:any){
    if(res && res.data)
      this.messageService.addMessage(res.data);
  }

  get activeDirChilds(): any[] {
    return this._activeDirChilds;
  }

  get rootDir(): any {
    return this._rootDir;
  }

  get activeDir(): any {
    return this._activeDir;
  }

  get activeDirMedia(): any[] {
    return this._activeDirMedia;
  }

  set activeDirMedia(media: any[]) {
    this._activeDirMedia = media;
    this._activeDirMediaChangedSub.next(media);
  }

  get activeDirChangedSub(): Subject<string> {
    return this._activeDirChangedSub;
  }

  get activeDirMediaChangedSub(): Subject<any> {
    return this._activeDirMediaChangedSub;
  }

  get activeDirBrothers(): any[] {
    return this._activeDirBrothers;
  }

  public hasMoreResults(): boolean {
    return this._activeDirMediaHasMoreResults;
  }
}

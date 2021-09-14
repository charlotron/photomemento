import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Constants} from "../../types/constants";
import {EnvVarsService} from "../env-vars.service";
import {ZOOM_LEVEL} from "../../types/zoom-levels-enu";

@Injectable({
  providedIn: 'root'
})
export class RestRequestService {
  constructor(private http: HttpClient, private evs: EnvVarsService) {
  }

  async getRootDirectory() {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.LIST_DIRECTORIES_PATH;
    return this.http.get(url, {params: {size: "1"}}).toPromise();
  }

  async getDirectoryById(id: string) {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.LIST_DIRECTORIES_PATH + id + Constants.URL_PATH_REPARATOR;
    return this.http.get(url).toPromise();
  }

  async getDirectoryChildsById(id: string) {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.LIST_DIRECTORIES_PATH + id + Constants.CHILDS_SUBPATH;
    return this.http.get(url).toPromise();
  }

  async recheckFiles() {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.RECHECK_FILES_PATH;
    return this.http.get(url).toPromise();
  }

  async reprocessFiles(id?: string, type?: string) {
    let options = (id || type) && {params: {}} || undefined;
    if (id) options.params['id'] = id;
    if (type) options.params['type'] = type;
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.REPROCESS_FILES_PATH;
    return this.http.get(url, options).toPromise();
  }

  async checkIntegrity() {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.CHECK_INTEGRITY_PATH;
    return this.http.get(url).toPromise();
  }

  async getDirectoryMediaById(id: string, page?: number) {
    let options;
    if (page) options = {params: {page: page}};
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.LIST_DIRECTORIES_PATH + id + Constants.MEDIA_SUBPATH;
    return this.http.get(url, options).toPromise();
  }

  async getMediaGlobal(page?: number) {
    let options;
    if (page) options = {params: {page: page}};
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.MEDIA_PATH;
    return this.http.get(url, options).toPromise();
  }

  async searchMediaGlobal(searchTerm: string, page?: number) {
    let options;
    if (page) options = {params: {page: page}};
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.SEARCH_PATH + searchTerm.replace("/"," ") + Constants.URL_PATH_REPARATOR;
    return this.http.get(url, options).toPromise();
  }

  async getStats(showStackTraceSummary: boolean) {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.STATS_PATH;
    let options;
    if (showStackTraceSummary) options = {params: {withThreads: true}};
    return this.http.get(url, options).toPromise();
  }

  async getMediaGeoData(zoomLevel: string) {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.GEO_DATA_PATH + zoomLevel + Constants.URL_PATH_REPARATOR;
    return this.http.get(url).toPromise();
  }

  async getLocation(zoomLevel: string) {
    let url = (await this.evs.getBackendBaseUrlWaitTillInit()) + Constants.LOCATION_PATH + zoomLevel + Constants.URL_PATH_REPARATOR;
    return this.http.get(url).toPromise();
  }
}

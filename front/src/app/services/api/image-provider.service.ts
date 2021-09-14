import {Injectable} from '@angular/core';
import {Constants} from "../../types/constants";
import {EnvVarsService} from "../env-vars.service";

@Injectable({
  providedIn: 'root'
})
export class ImageProviderService {

  constructor(private evs:EnvVarsService) {}

  getCachedMediaUrl(path: any) {
    if (!path) return;
    let baseUrl =this.evs.getBackendBaseUrlAssumeInitOk();
    return baseUrl + Constants.CACHED_MEDIA_PATH + path.replace(/^.*[\\/]/, "");
  }

  getDownloadImgUrl(id: any) {
    if (!id) return;
    let baseUrl =this.evs.getBackendBaseUrlAssumeInitOk();
    return baseUrl+Constants.IMG_PATH + id + "/";
  }

  getStreamingVidUrl(id: any) {
    if (!id) return;
    let baseUrl =this.evs.getBackendBaseUrlAssumeInitOk();
    return baseUrl+Constants.VID_PATH + id + "/";
  }

  getDownloadUrl(activeMedia: any) {
    return activeMedia.type=="PHOTO"?
      this.getDownloadImgUrl(activeMedia.id):
      this.getStreamingVidUrl(activeMedia.id);
  }

  getApiUrl(id: any) {
    if (!id) return;
    let baseUrl =this.evs.getBackendBaseUrlAssumeInitOk();
    return baseUrl+Constants.MEDIA_PATH + id + "/";
  }
}

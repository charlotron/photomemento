import {Injectable} from '@angular/core';
import {DirectoryProviderService} from "../api/directory-provider.service";
import {Subject} from "rxjs";
import {DatePipe} from "@angular/common";
import {Constants} from "../../types/constants";
import {MediaProviderService} from "../api/media-provider.service";
import {SpinnerStatusService} from "./spinner-status.service";
import {EnvVarsService} from "../env-vars.service";

@Injectable({
  providedIn: 'root'
})
export class GalleryService {

  private _media: any[] = [];
  private _activeMedia: any;
  private _mediaSub: Subject<any> = new Subject<any>();
  private _activeMediaSub: Subject<any> = new Subject<any>();
  private _activeProviderIsMedia = false;
  private _previousProcessedMedia: any;
  private readonly _spinnerStatus: any;
  private _checkedForMedia: boolean;
  private _isError: boolean;
  private geoGroupKeys: string[];

  constructor(
    public spinnerStatusSvc: SpinnerStatusService,
    public dirProvSvc: DirectoryProviderService,
    public mediaProvSvc: MediaProviderService,
    private datePipe: DatePipe,
    private envVarsSvc: EnvVarsService) {

    this._spinnerStatus = this.spinnerStatusSvc.newSpinnerStatus();

    dirProvSvc.activeDirChangedSub.subscribe(() => {
      this.onStartRequestingNewData(true);
    });
    dirProvSvc.activeDirMediaChangedSub.subscribe(async (media) => {
      await this.onMediaWasUpdated(media);
    });
    mediaProvSvc.mediaInitSub.subscribe(() => {
      this.onStartRequestingNewData(true);
    });
    mediaProvSvc.mediaChangedSub.subscribe(async (media) => {
      await this.onMediaWasUpdated(media);
    });
  }

  private onStartRequestingNewData(activeProviderIsMedia: boolean) {
    this._media = [];
    this._checkedForMedia = false;
    this._previousProcessedMedia = undefined;
    this._activeProviderIsMedia = activeProviderIsMedia;
    this.spinnerStatus.waitEventStarted();
  }

  private async onMediaWasUpdated(media) {
    this._isError = media == null;
    if (media != null) {
      await this.generateGroups(media);
      this._activeMedia = undefined;
      this._mediaSub.next(media);
      this._checkedForMedia = true;
    }
    this.spinnerStatus.waitEventFinished();
  }

  /**
   * Generate grouped data by date, then location, then image
   * @param media
   */
  private async generateGroups(media: any[]) {
    for (const mediaElement of media) {
      let dateGroup = this.generateOrGetDateGroup(mediaElement);
      let locationGroup = await this.generateOrGetLocationGroup(mediaElement, dateGroup);
      locationGroup.contents.push(mediaElement);
      //TODO prev and next has to be fixed
      if (this._previousProcessedMedia)
        this._previousProcessedMedia.next = mediaElement;
      mediaElement.prev = this._previousProcessedMedia;
      this._previousProcessedMedia = mediaElement;
    }
  }

  /**
   * Generates groups based by date, media is a list of
   * {
   *   key:"2020-01-20", //Date for the images
   *   contents:[]       //Locations for this date
   * }
   * @param mediaElement
   * @private
   */
  private generateOrGetDateGroup(mediaElement) {
    let dateGroupKey = this.resolveDateGroupKey(mediaElement);
    let dateGroup;
    if (!this._media.length || this._media[this._media.length - 1].key !== dateGroupKey) {
      dateGroup = {key: dateGroupKey, contents: []};
      this.media.push(dateGroup);
    } else
      dateGroup = this._media[this._media.length - 1];
    return dateGroup;
  }

  /**
   * Parses the date to generate a unique key for it with the format yyyy-MM-dd
   * @param mediaElement
   * @private
   */
  private resolveDateGroupKey(mediaElement) {
    return this.datePipe.transform(mediaElement.shotDate, 'yyyy-MM-dd');
  }

  /**
   * Generates or resolves a location group based on image location
   * @param mediaElement
   * @param dateGroup
   * @private
   */
  private async generateOrGetLocationGroup(mediaElement, dateGroup) {
    let locationGroupKey = await this.resolveLocationGroupKey(mediaElement);
    let locationGroup = dateGroup.contents.find(el => el.key == locationGroupKey);
    if (locationGroup) return locationGroup;
    locationGroup = {
      key: locationGroupKey,
      contents: []
    };
    dateGroup.contents.push(locationGroup);
    dateGroup.contents.sort((x, y) => {
      if (x.key < y.key) return -1;
      else if (x.key > y.key) return 1;
      return 0;
    });
    return locationGroup;
  }

  /**
   * Resolve location group key and inserts data into the object
   * @param mediaElement
   * @private
   */
  private async resolveLocationGroupKey(mediaElement) {
    let locationKey;
    if (mediaElement && mediaElement.geoData && mediaElement.geoData.zoomLevels) {
      let zoomLevels=mediaElement.geoData.zoomLevels;
      if(zoomLevels.highLevel) return zoomLevels.highLevel;
      if(zoomLevels.mediumLevel) return zoomLevels.mediumLevel;
      if(zoomLevels.lowLevel) return zoomLevels.lowLevel;
    }
    if (!locationKey) locationKey = Constants.OTHER;
    return locationKey;
  }

  private async getGeoGroupKeys() {
    if (this.geoGroupKeys) return this.geoGroupKeys;

    let geoGroupsStr: string = await this.envVarsSvc.getEnv(Constants.ENV_PROP_GEO_GROUPS);
    this.geoGroupKeys = !geoGroupsStr ?
      [] :
      geoGroupsStr
        .split(',')
        .map(val => val.trim())
        .filter(val => val);
    return this.geoGroupKeys;
  }

  private resolveAddressKey(mediaElement, key: string) {
    return mediaElement.address.addressElements.find((m) => m.key == key);
  }

  async nextMediaPage() {
    if (this._activeProviderIsMedia)
      if (this.mediaProvSvc.hasMoreResults()) {
        this.spinnerStatus.waitEventStarted();
        await this.mediaProvSvc.nextMediaPage();
      } else if (this.dirProvSvc.hasMoreResults()) {
        this.spinnerStatus.waitEventStarted();
        await this.dirProvSvc.nextMediaPage();
      }
  }

  set activeMedia(activeMedia: any) {
    this._activeMedia = activeMedia;
    this._activeMediaSub.next(activeMedia);
  }

  get activeMedia(): any {
    return this._activeMedia;
  }

  get media(): any {
    return this._media;
  }

  get mediaSub(): Subject<any> {
    return this._mediaSub;
  }

  get spinnerStatus(): any {
    return this._spinnerStatus;
  }

  get checkedForMedia(): boolean {
    return this._checkedForMedia;
  }

  get isError(): boolean {
    return this._isError;
  }

  get activeMediaSub(): Subject<any> {
    return this._activeMediaSub;
  }
}

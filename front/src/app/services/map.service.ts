import {Injectable} from '@angular/core';
import {Constants} from "../types/constants";
import {LatLngTuple} from "leaflet";
import {RestRequestService} from "./api/rest-request.service";
import {ReplaySubject} from "rxjs";
import {ZOOM_LEVEL} from "../types/zoom-levels-enu";
import {EnuUtils} from "../utils/enu-utils";

// noinspection JSUnfilteredForInLoop
@Injectable({
  providedIn: 'root'
})
export class MapService {
  private _locations: any;
  private _locationsUpdatedSub: ReplaySubject<any> = new ReplaySubject<any>();

  constructor(private restRequestSvc: RestRequestService) {
  }

  updateLocations() {
    EnuUtils.forEach(ZOOM_LEVEL, (zoomLevel) => {
      Promise.all([
        this.restRequestSvc.getMediaGeoData(zoomLevel),
        this.restRequestSvc.getLocation(zoomLevel)
      ])
        .then(results => {
          if (!results || results.length !== 2 || !results[0] || !results[1]) return;
          if (!this._locations) this._locations = {};
          let geoDataList: any = results[0];
          let locations: any = results[1];

          //Save default locations (provided from media geoData, took from first result, but could be low accurated - ie: spain could be located at galicia but this is not valid, center is madrid)
          this._locations[zoomLevel] = geoDataList;

          //Look for real coordinates for location
          for (let geoDataEl of geoDataList) {
            let location = locations.find((location) => location.name === geoDataEl.name);
            if (location){
              geoDataEl.latitude = location.latitude;
              geoDataEl.longitude = location.longitude;
            }
          }
          //Notify
          this._locationsUpdatedSub.next(zoomLevel);
        })
        .catch(e => console.error("Unable to retrieve location results for: " + zoomLevel, e))
    });
  }

  async getCenterOfMap(): Promise<LatLngTuple> {
    try {
      const pos: any = await new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject);
      });

      return [pos.coords.latitude, pos.coords.longitude];
    } catch (e) {
      return Constants.DEFAULT_COORDINATES;
    }
  }

  get locations() {
    return this._locations;
  }

  get locationsUpdatedSub(): ReplaySubject<any> {
    return this._locationsUpdatedSub;
  }

  getLocations(zoomLevel: any) {
    return this._locations[zoomLevel];
  }
}

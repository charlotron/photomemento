import {AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {MapService} from "../../../services/map.service";
import 'leaflet.heat/dist/leaflet-heat.js';
import {ZOOM_LEVEL} from "../../../types/zoom-levels-enu";
import {EnuUtils} from "../../../utils/enu-utils";
import {Constants} from "../../../types/constants";
import {Router} from "@angular/router";

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, AfterViewInit {

  private static ZOOM_RADIUS = {
    3: {r: 20, z: ZOOM_LEVEL.LOW},
    4: {r: 40, z: ZOOM_LEVEL.LOW},
    5: {r: 30, z: ZOOM_LEVEL.MEDIUM},
    6: {r: 60, z: ZOOM_LEVEL.MEDIUM},
    7: {r: 80, z: ZOOM_LEVEL.MEDIUM},
    8: {r: 20, z: ZOOM_LEVEL.HIGH},
    9: {r: 20, z: ZOOM_LEVEL.HIGH},
    10: {r: 20, z: ZOOM_LEVEL.HIGH},
  };

  private map;
  private actualMapZoom: number;
  private actualZoomLevel: string; //LOW, MEDIUM, HIGH
  private myMarkers: L.LayerGroup = L.layerGroup();

  constructor(
    private mapSvc: MapService,
    private route:Router) {
  }

  ngOnInit() {
    //Call to update locations, a subscription event will be fired and paint layers later
    this.mapSvc.updateLocations();
  }

  /**
   * Initializes the map, it is fired by ngAfterViewInit
   * @param center
   * @private
   */
  private initMap(center: L.LatLngTuple): void {
    this.map = L.map('map', {
      center,
      zoom: 3,
      minZoom: 3,
      maxZoom: 10
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      subdomains: 'abcd',
      maxZoom: 19
    }).addTo(this.map);

    /*
        (L as any).heatLayer([
          [
            Constants.DEFAULT_COORDINATES[0],
            Constants.DEFAULT_COORDINATES[1],
            50
          ],
          [
            48.210033, 16.363449, //VIENA
            1000
          ],
          [
            48.864716, 2.349014, //PARIS
            200
          ],
          [
            28.291565, -16.629129, //Canarias
            500
          ]
        ], {radius: 15, max:0.3,  gradient:{0.1: 'green', 0.4: 'blue', 0.65: 'lime', 1: 'red'}}) //radius: 15, blur: 5,minOpacity:0.5
          .addTo(this.map);
        ;*/

    this.map.on('zoomend', () => this.onZoomChanged());

    //Repaint layers
    this.mapSvc.locationsUpdatedSub.subscribe((updatedZoomLevel) => this.onUpdatedLayer(updatedZoomLevel))

  }

  /**
   * This is called when api responds, so there is data for this zoom level!
   * @param updatedZoomLevel (LOW, MEDIUM, HIGH)
   */
  private onUpdatedLayer(updatedZoomLevel) {
    if (this.getActualZoomLevel() !== updatedZoomLevel) return; //Not the right layer so don't do anything
    this.repaintMapMarkers();
    return true;
  }

  /**
   * This is called zoom in the map changed
   */
  private onZoomChanged() {
    let zoom = this.map.getZoom();
    console.log("Zoom is: ", zoom);

    //Zoom has changed?
    if (this.actualMapZoom === zoom) return;

    //Check if repaint or resize
    if (this.getActualZoomLevel() !== this.actualZoomLevel)
      this.repaintMapMarkers();
    else
      this.resizeMapMarkers();
  }

  /**
   * Resizes markers
   */
  private resizeMapMarkers() {
    /*let actualMapZoom = this.getActualZoom();

    this.myMarkers.eachLayer(function (marker: any) {
      //marker.setRadius(MapComponent.ZOOM_RADIUS[actualMapZoom].r);
      //Need to repaint tooltip as long as offset cannot be updated
      /*let tooltip = marker.getTooltip();
      let oldContent: string = tooltip.getContent();
      tooltip.remove();
      MapComponent.addToolTip(marker, oldContent, actualMapZoom);
    });*/

    this.actualMapZoom = this.getActualZoom();
  }

  private static addToolTip(marker: L.Marker, oldContent: string, actualMapZoom: number): L.Marker {
    marker.bindTooltip(oldContent,
      {
        //permanent: true,
        direction: 'right',
        //offset: new L.Point(MapComponent.ZOOM_RADIUS[actualMapZoom].r, 0),
        offset: [10, 0], //X,Y
        opacity: 0.7
      });
    return marker;
  }

  /**
   * Updates current map from current actual zoom level
   */
  private repaintMapMarkers() {
    let actualZoomLevel = this.getActualZoomLevel();
    let actualMapZoom = this.getActualZoom();

    let locations = this.mapSvc.getLocations(actualZoomLevel);
    if (!locations) return;

    //Update the map markers
    this.myMarkers.clearLayers();
    locations.forEach((location) => {
      /* let marker = L.circleMarker(
         [location.latitude, location.longitude],
         {radius: MapComponent.ZOOM_RADIUS[actualMapZoom].r}
       );*/
      let marker = L.marker(
        [location.latitude, location.longitude],
        {
          icon: L.icon({
            iconUrl: Constants.LOCAL_ASSET_IMAGES + "map-marker-icon.png",
            iconSize: [24, 24], //X,Y
            iconAnchor: [12, 24] //X,Y
          })
        }
      );
      MapComponent.addToolTip(marker, location.name + "(" + location.count + ")", actualMapZoom);
      marker.on("click", () => {
        this.onMapMarkerClick(location, actualZoomLevel)
      })
      marker.addTo(this.myMarkers);
    });
    this.myMarkers.addTo(this.map);

    //Update zoom level
    this.actualZoomLevel = actualZoomLevel;
    this.actualMapZoom = actualMapZoom;
  }


  ngAfterViewInit(): void {
    (async () => {
      this.initMap(await this.mapSvc.getCenterOfMap());
    })()
  }

  //
  // onMapReady(map) {
  //   const heat = L.heatLayer(
  //     [
  //       [
  //         -37.8839,
  //         175.3745188667,
  //         "571"
  //       ],
  //       [
  //         -37.8869090667,
  //         175.3657417333,
  //         "486"
  //       ]
  //     ],
  //     {}
  //   ).addTo(map);
  // }


  private getActualZoom(): number {
    return this.map.getZoom();
  }

  private getActualZoomLevel(): string {
    return EnuUtils.getName(ZOOM_LEVEL, MapComponent.ZOOM_RADIUS[this.getActualZoom()].z)
  }

  private onMapMarkerClick(location: any, actualZoomLevel: string) {
    console.log("Clicked on: ", location, " zoom level: ", actualZoomLevel);
    this.route.navigate(['/'],{queryParams:{q:location.name}});
  }
}

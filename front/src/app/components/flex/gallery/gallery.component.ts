import {Component, HostListener, OnInit, QueryList, ViewChildren} from '@angular/core';
import {GalleryService} from "../../../services/components/gallery.service";
import {ImageProviderService} from "../../../services/api/image-provider.service";
import {LoadedDirective} from "../../../directives/loaded.directive";
import {forkJoin} from "rxjs";
import {Constants} from "../../../types/constants";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-gallery',
  templateUrl: './gallery.component.html',
  styleUrls: ['./gallery.component.scss']
})
export class GalleryComponent implements OnInit {
  public readonly constants = Constants;
  private static readonly TRIGGER_AT_PX_TO_THE_BOTTOM: number = 400; //original: 128;
  private triggered: boolean = false;
  private static currentYear = new Date().getFullYear() + "";

  @ViewChildren(LoadedDirective) images: QueryList<LoadedDirective>; //This directive emits an event when images are loaded

  constructor(public gallerySvc: GalleryService,
              public imgProviderService: ImageProviderService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  async ngOnInit(): Promise<any> {
    this.gallerySvc.mediaSub.subscribe(async () => {
      this.onLoadImagesRun(async () => {
        this.triggered = false;
        this.scrollHandler();
      }); //When all images are loaded check if we have to load next group of images
    });
  }

  onLoadImagesRun(callback) {
    setTimeout(async () => {
      await forkJoin(this.images.map(imgDir => imgDir.loadComplete)).toPromise();
      callback();
    }, 0);
  }

  onImageClick(activeMedia: any[]) {
    this.gallerySvc.activeMedia = activeMedia;
    return false;
  }

  @HostListener('window:scroll', ['$event'])
  @HostListener('window:resize', ['$event'])
  async scrollHandler(event?: Event) {
    //This event triggers when scroll is "TRIGGER_AT_PX_TO_THE_BOTTOM" px distance to the bottom, and triggers only once per results load
    if (!this.triggered && document.body.scrollHeight - (window.innerHeight + window.scrollY) <= GalleryComponent.TRIGGER_AT_PX_TO_THE_BOTTOM) {
      this.triggered = true;
      await this.gallerySvc.nextMediaPage();
    }
  }

  countKeys(obj) {
    return Object.keys(obj).length;
  }

  isCurrentYear(key: string) {
    return GalleryComponent.currentYear == key;
  }

  hasMedia() {
    return Object.keys(this.gallerySvc.media).length !== 0;
  }

  getScaledWidth(media, height) {
    if (!media.widthHeightRatio || !height) return null;
    let width = Math.floor(media.widthHeightRatio * height);
    return width % 2 == 0 ? width : width + 1;
  }

  onMediaClicked(media: any) {
    this.gallerySvc.activeMedia = media;
    this.router.navigate(
      [],
      {
        relativeTo:
          this.route.children && this.route.children.length == 1 ?
            this.route.children[0] :
            this.route,
        queryParams: {
          preview: media?.id
        },
        queryParamsHandling: 'merge',
      });
    //this.router.navigate(['prv',media?.id], {relativeTo: this.route.children && this.route.children.length==1?this.route.children[0]:this.route});
  }
}

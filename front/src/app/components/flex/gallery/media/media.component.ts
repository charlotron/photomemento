import {Component, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {GalleryService} from "../../../../services/components/gallery.service";
import {ImageProviderService} from "../../../../services/api/image-provider.service";
import {KeyEventsService} from "../../../../services/api/key-events.service";
import {Subject, Subscription} from "rxjs";
import {Constants} from "../../../../types/constants";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-media',
  templateUrl: './media.component.html',
  styleUrls: ['./media.component.scss']
})
export class MediaComponent implements OnInit {

  private static readonly SLIDESHOW_TIMEOUT_PASS_SLIDE: number = 5;

  @ViewChild('frame') frame: any;

  private interval: any;
  public slideshowCountDown: number;
  public slideshowMode: boolean;
  private keyEventSub: Subscription;
  mediaHeight: number;
  mediaWidth: number;

  constructor(
    private keyEventsSvc: KeyEventsService,
    public gallerySvc: GalleryService,
    public imageProvSvc: ImageProviderService) {
  }

  ngOnInit() {
    this.keyEventSub = this.keyEventsSvc.keyPress.subscribe((event: KeyboardEvent) => {
      if (this.gallerySvc.activeMedia)
        this.processKeyPress(event);
    });
    this.onResize();
  }

  onGoBackArrowClicked(fromHistoryEvent?: boolean) {
    if (!fromHistoryEvent) {
      window.history.back();
      return;
    }
    this.gallerySvc.activeMedia = undefined;
    this.slideshowModeStop();
  }

  slideshowModeStart() {
    if (!this.gallerySvc.activeMedia.next) return;
    this.slideshowMode = true;
    this.slideshowPlay();
  }

  slideshowPlay() {
    if (this.interval ||
      !this.gallerySvc.activeMedia ||
      !this.gallerySvc.activeMedia.next)
      clearInterval();

    this.slideshowCountDown = MediaComponent.SLIDESHOW_TIMEOUT_PASS_SLIDE;

    this.interval = setInterval(() => {
      if (!this.gallerySvc.activeMedia ||
        !this.gallerySvc.activeMedia.next)
        this.slideshowModeStop();

      if (this.slideshowCountDown == 1) {
        this.gallerySvc.activeMedia = this.gallerySvc.activeMedia.next;
        this.slideshowCountDown = MediaComponent.SLIDESHOW_TIMEOUT_PASS_SLIDE;
      } else if (!this.slideshowCountDown) {
        this.slideshowCountDown = MediaComponent.SLIDESHOW_TIMEOUT_PASS_SLIDE;
      } else
        this.slideshowCountDown--;
    }, 1000, 1000);
  }

  slideshowPause() {
    if (!this.interval) return;
    clearInterval(this.interval);
    this.interval = undefined;
    this.slideshowCountDown = undefined;
  }

  slideshowModeStop() {
    this.slideshowPause();
    this.slideshowMode = false;
  }

  isSlideshowPaused() {
    return this.slideshowMode && !this.interval;
  }

  slideshowResetCountdown() {
    this.slideshowCountDown = undefined;
  }

  private processKeyPress(event: KeyboardEvent) {
    if (!this.gallerySvc.activeMedia) return;
    event.stopPropagation();
    event.preventDefault();
    if (event.key == Constants.KEY_ARROW_LEFT)
      this.prevPhoto();
    else if (event.key == Constants.KEY_ARROW_RIGHT)
      this.nextPhoto();
    else if (event.key == Constants.KEY_ESCAPE)
      this.onGoBackArrowClicked();
    else if (event.key == Constants.KEY_SPACE) {
      if (!this.gallerySvc.activeMedia.next)
        return;
      if (this.slideshowMode) {
        if (this.isSlideshowPaused())
          this.slideshowPlay();
        else
          this.slideshowPause();
      } else
        this.slideshowModeStart();
    }
  }

  /*ngOnDestroy(): void {
    if(this.keyEventSub)
      this.keyEventSub.unsubscribe();
  }*/

  prevPhoto() {
    if (!this.gallerySvc.activeMedia.prev) return;
    this.gallerySvc.activeMedia = this.gallerySvc.activeMedia.prev;
    this.slideshowResetCountdown();
  }

  nextPhoto() {
    if (!this.gallerySvc.activeMedia.next) return;
    this.gallerySvc.activeMedia = this.gallerySvc.activeMedia.next;
    this.slideshowResetCountdown();
  }

  @HostListener('window:resize', ['$event'])
  public onResize() {
    this.mediaWidth = window.innerWidth;
    this.mediaHeight = window.innerHeight;
  }

  //Go back button close this
  @HostListener('window:popstate', ['$event'])
  onPopState(event) {
    if (!this.gallerySvc.activeMedia) return;
    this.onGoBackArrowClicked(true);
  }

  swipe($event: any) {
    console.log("evento pillado");
  }

  changeQuality() {
    this.gallerySvc.activeMedia.previewSrc =
      this.gallerySvc.activeMedia.previewSrc ?
        undefined :
        this.imageProvSvc.getDownloadUrl(this.gallerySvc.activeMedia);
  }

  getImage() {
    return this.gallerySvc.activeMedia.previewSrc ||
      this.imageProvSvc.getCachedMediaUrl(this.gallerySvc.activeMedia?.media?.n);
  }

  hasPreviewSrcSet():boolean{
    return !!this.gallerySvc.activeMedia.previewSrc;
  }
}

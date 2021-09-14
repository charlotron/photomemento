import {Directive, EventEmitter, HostListener, Output} from '@angular/core';

@Directive({
  selector: "[onLoaded]",
})
export class LoadedDirective {
  @Output() loaded = new EventEmitter();
  @Output() loadError = new EventEmitter();
  @Output() loadComplete = new EventEmitter();

  @HostListener('load')       //This is for images
  @HostListener('loadeddata') //This is for videos
  imageLoaded() {
    this.loaded.emit();
    this.loaded.complete();
    this.loadComplete.emit();
    this.loadComplete.complete();
  }

  @HostListener('error')
  imageLoadError(){
    this.loadError.emit();
    this.loadError.complete();
    this.loadComplete.emit();
    this.loadComplete.complete();
  }
}

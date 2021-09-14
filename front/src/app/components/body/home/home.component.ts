import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Params, Router} from "@angular/router";
import {MediaProviderService} from "../../../services/api/media-provider.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {
  private retrying: any;
  private retryingInterval: any;
  searchTerm: string;
  private previousQ: string;
  private first:boolean=true;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private mediaProvSvc: MediaProviderService) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(async (params: Params) => {
      if (this.first||this.previousQ !== params.q) {
        this.first=false;
        this.previousQ = params.q;
        this.mediaProvSvc.init(params.q);
        this.searchTerm = params.q;
        if (!this.retrying)
          await this.checkMedia();
      }
    });
  }

  //TODO: This is also done on dirs.component, look for a better approach (dont forget the OnDestroy)
  //TODO: This only works for first request not for successive pages
  private async checkMedia() {
    try {
      await this.mediaProvSvc.checkMedia();
      this.retrying = false;
    } catch (e) {
      console.error("Error requesting media, due to: ", e);
      this.retrying = true;
      this.retryingInterval = setTimeout(async () => {
        console.log("Rechecking..");
        await this.checkMedia()
      }, 5000);
    }
  }

  ngOnDestroy(): void {
    if (this.retrying) {
      clearInterval(this.retryingInterval);
      this.retrying = false;
    }
    this.mediaProvSvc.clear();
  }

  removeFilters() {
    this.router.navigate(['/']);
  }
}

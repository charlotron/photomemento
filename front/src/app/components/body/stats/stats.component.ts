import {Component, OnDestroy, OnInit} from '@angular/core';
import {RestRequestService} from "../../../services/api/rest-request.service";
import {interval} from "rxjs";
import {Constants} from "../../../types/constants";

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: ['./stats.component.scss']
})
export class StatsComponent implements OnInit, OnDestroy {
  stats: any;
  private interval: any;
  autoRefreshChecked: any = false;
  showStackTraceSummary: boolean;
  refreshIntervalCountDown: number;

  constructor(private restRequestSvc: RestRequestService) {
  }

  async ngOnInit() {
    await this.checkStats();
  }

  ngOnDestroy(): void {
    this.stopChecking();
  }

  private stopChecking() {
    if (!this.interval) return;
    clearInterval(this.interval)
    this.interval = undefined;
  }

  onCheckStatsClick(){
    if(this.interval)
      this.refreshIntervalCountDown=0;
    else
      this.checkStats();
  }

  async checkStats() {
    this.stats = await this.restRequestSvc.getStats(!!this.showStackTraceSummary);
    console.debug("Reloaded stats! ^^")
  }

  autoRefreshToggleChange(event: any) {
    event.preventDefault();
    event.stopPropagation();
    this.autoRefreshChecked = !this.autoRefreshChecked;
    this.stopChecking();
    if (!this.autoRefreshChecked) return;
    this.refreshIntervalCountDown = Constants.STATS_CHECK_INTERVAL/1000;
    this.interval = setInterval(async () => {
      if(this.refreshIntervalCountDown)
        this.refreshIntervalCountDown--;
      if (this.refreshIntervalCountDown <= 0) {
        await this.checkStats();
        this.refreshIntervalCountDown = Constants.STATS_CHECK_INTERVAL/1000;
      }
    }, 1000);
  }

  async toggleShowStackTrace() {
    this.showStackTraceSummary = !this.showStackTraceSummary;
    await this.checkStats()
  }

  onProcessorsPlay() {

  }

  onProcessorsStop() {

  }
}

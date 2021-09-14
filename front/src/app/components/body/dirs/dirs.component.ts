import {Component, OnDestroy, OnInit} from '@angular/core';
import {DirectoryProviderService} from "../../../services/api/directory-provider.service";
import {ActivatedRoute, Params, Route} from "@angular/router";
import {BreadCrumbsDataService} from "../../../services/components/bread-crumbs-data.service";
import {SectionsService} from "../../../services/sections.service";
import {SpinnerStatusService} from "../../../services/components/spinner-status.service";
import {Constants} from "../../../types/constants";

@Component({
  selector: 'app-dirs',
  templateUrl: './dirs.component.html',
  styleUrls: ['./dirs.component.scss']
})
export class DirsComponent implements OnInit, OnDestroy {
  private _activeDirId: any;
  private retrying: any;
  private retryingInterval: any;
  private section: Route;

  constructor(
    public dirProvSvc: DirectoryProviderService,
    private spinnerSvc: SpinnerStatusService,
    private route: ActivatedRoute,
    private breadCrumbsDataServiceService: BreadCrumbsDataService,
    private sectionsService: SectionsService) {
  }

  async ngOnInit() {
    this.route.children.forEach(child => child.params.subscribe(async (params: Params) => {
      this._activeDirId = params.id == Constants.ROOT ? undefined : params.id;
      if (!this.retrying)
        await this.changeActiveDir();
    }))
  }

  private async changeActiveDir() {
    try {
      await this.dirProvSvc.activeDirChanged(this._activeDirId);
      this.section=this.sectionsService.activeSection;
      this.retrying = false;
      let hierarchy = this.dirProvSvc.activeDir.hierarchy;
      this.configureBreadcrumbs(hierarchy, this.dirProvSvc.activeDir);
      this.configureSections(hierarchy, this.dirProvSvc.activeDir, this.dirProvSvc.activeDirChilds, this.dirProvSvc.activeDirBrothers);
    } catch (e) {
      console.error("Error processing change of active dir id: ", this._activeDirId, ", due to: ", e);
      this.retrying = true;
      this.retryingInterval = setTimeout(async () => {
        console.log("Rechecking..");
        await this.changeActiveDir()
      }, 5000);
    }
  }

  /**
   * Generates breadcrumb content over the body
   * @param hierarchy
   * @param activeDir
   */
  configureBreadcrumbs(hierarchy: any[], activeDir: any) {
    if (!hierarchy) return;
    //show ".." if depth is 2 or more
    if (hierarchy.length >= 3) //Show ".."
      this.breadCrumbsDataServiceService.registerText(undefined, undefined, "..");

    //show "parentDir" as link
    if (hierarchy.length >= 2) { //Show .. >> parentDir >> currentDir
      let previousDir = hierarchy[hierarchy.length - 1];
      let path = this.sectionsService.activeSection.path + "/" + previousDir.id;
      this.breadCrumbsDataServiceService.registerText(path, undefined, previousDir.name);
    }

    //Show "currentDir" as text not link
    if (hierarchy.length >= 1)
      this.breadCrumbsDataServiceService.registerText(undefined, undefined, activeDir.name);
  }

  /**
   * Fills the sections (sidebar) with directory hierarchy
   * @param hierarchy
   * @param activeDir
   * @param activeDirChilds
   */
  configureSections(hierarchy: any[], activeDir: any, activeDirChilds: any[], activeDirBrothers: any[]) {
    if (!this.sectionsService.activeSection || !this.sectionsService.activeSection.data) return;
    let activeSection = this.sectionsService.activeSection;
    this.sectionsService.clearSubsections(activeSection);

    let actSect;
    let prev = this.addSubsection(activeSection, this.dirProvSvc.rootDir.id, "root", !hierarchy, true);
    if (!hierarchy)
      actSect = prev;
    if (hierarchy) {
      if (hierarchy.length > 1)
        //Printing all parents hierarchy
        for(let i=1;i<hierarchy.length;i++)
          prev = this.addSubsection(prev, hierarchy[i].id, hierarchy[i].name, false, true);

      if (hierarchy.length > 0) {
        //Show brothers and itself
        if (activeDirBrothers)
          activeDirBrothers.forEach(brother => {
            let isActive: boolean = brother.id == activeDir.id;
            let cur = this.addSubsection(prev, brother.id, brother.name, isActive, isActive);
            if (isActive)
              actSect = cur;
          });
      }
    }
    if (activeDirChilds)
      activeDirChilds.forEach(child =>
        this.addSubsection(actSect, child.id, child.name, false, false));
  }

  private addSubsection(prev, id, name, isActive, updatePrev) {
    let res = this.sectionsService.addSectionSubsection(
      prev,
      this.sectionsService.activeSection.path + "/" + id,
      undefined,
      name,
      isActive);
    return updatePrev && res || prev;
  }

  ngOnDestroy(): void {
    if (this.retrying) {
      clearInterval(this.retryingInterval);
      this.retrying = false;
    }
    this.dirProvSvc.clearActiveDir();
    if (!this.sectionsService.activeSection || !this.sectionsService.activeSection.data) return;
    this.sectionsService.clearSubsections(this.section);
  }

  async reprocessDir() {
    await this.dirProvSvc.reprocessActiveDir()
  }
}

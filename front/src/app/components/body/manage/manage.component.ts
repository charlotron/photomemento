import { Component, OnInit } from '@angular/core';
import {ManageService} from "../../../services/api/manage.service";
import {Constants} from "../../../types/constants";

@Component({
  selector: 'app-manage',
  templateUrl: './manage.component.html',
  styleUrls: ['./manage.component.scss']
})
export class ManageComponent implements OnInit {

  public constants=Constants;

  constructor(private manageService:ManageService) {}

  ngOnInit(): void {}

  async onRecheckFiles(){
    await this.manageService.recheckFiles();
  }

  async onReprocessFiles(fileType?:string) {
    await this.manageService.reprocessFiles(fileType);
  }

  async onCheckIntegrity() {
    await this.manageService.checkIntegrity();
  }
}

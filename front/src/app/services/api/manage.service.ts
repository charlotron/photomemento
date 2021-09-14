import {Injectable} from '@angular/core';
import {RestRequestService} from "./rest-request.service";
import {MessageService} from "../components/message.service";

@Injectable({
  providedIn: 'root'
})
export class ManageService {

  constructor(
    private restRequestService:RestRequestService,
    private messageService:MessageService) {}

  ngOnInit(): void {}

  async recheckFiles(){
    await this.processResponseAsMessage(await this.restRequestService.recheckFiles());
  }

  async reprocessFiles(fileType?:string){
    await this.processResponseAsMessage(await this.restRequestService.reprocessFiles(null, fileType));
  }

  async checkIntegrity(){
    await this.processResponseAsMessage(await this.restRequestService.checkIntegrity());
  }

  private async processResponseAsMessage(res:any){
    if(res && res.data)
      this.messageService.addMessage(res.data);
  }
}

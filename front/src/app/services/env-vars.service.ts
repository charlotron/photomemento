import {Injectable} from '@angular/core';
import {Constants} from "../types/constants";
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs";
import {take} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class EnvVarsService{
  private _defaultEnvs: Object;
  private _localEnvs: Object;
  private _envs: Object;
  private _isInit:boolean;
  private _onInitObs:ReplaySubject<any>=new ReplaySubject<any>(1);

  constructor(private http: HttpClient) {
    this.init().then(()=>{}); //NOSONAR
  }

  async init(){
    if(this._isInit) return;
    this._defaultEnvs = await this.getDefaultEnvVars();
    this._localEnvs = await this.getLocalEnvVars();
    this._envs={...this._defaultEnvs, ...this._localEnvs };
    this._isInit=true;
    this._onInitObs.next(true);
  }

  private async getDefaultEnvVars() {
    return this.http.get(Constants.LOCAL_DEFAULT_ENV_VARS).toPromise();
  }

  private async getLocalEnvVars() {
    return this.http.get(Constants.LOCAL_LOCAL_ENV_VARS).toPromise();
  }

  get envs():Object{
    return this._envs;
  }

  get isInit():boolean{
    return this._isInit;
  }

  get onInitObs():ReplaySubject<any>{
    return this._onInitObs;
  }

  async waitTillInit(){
    return this._onInitObs.pipe(take(1)).toPromise();
  }

  async getEnv(key:string){
    await this.waitTillInit();
    return this._envs[key];
  }

  async getBackendBaseUrlWaitTillInit(){
    return this.getEnv(Constants.ENV_PROP_BACKEND_BASE_URL);
  }

  getBackendBaseUrlAssumeInitOk(){
    return this._envs[Constants.ENV_PROP_BACKEND_BASE_URL];
  }
}

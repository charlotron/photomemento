export class EnuUtils {

  public static getNames(enu: any): string[] {
    let res: string[] = [];
    if (enu)
      for (let key in enu)
        if (EnuUtils.isKey(key))
          res.push(key);
    return res;
  }

  public static getValues(enu: any): number[] {
    let res: number[] = [];
    if (enu)
      for (let val in enu)
        if (EnuUtils.isValue(val) && typeof val==="number")
          res.push(val);
    return res;
  }

  public static getName(enu: any, val: any):string {
    if (!enu) return null;

    if (typeof enu === "string") return enu;
    return enu[val];
  }

  public static getValue(enu: any, val: any):number {
    if (!enu) return null;
    if (typeof enu === "number") return enu;
    return enu[val];
  }

  public static isKey(val: any) {
    return isNaN(Number(val));
  }

  public static isValue(val: any) {
    return !isNaN(Number(val));
  }

  public static forEach(enu:any, func:(key:string,val:number)=>void){
    for(let key of EnuUtils.getNames(enu))
      func(key,EnuUtils.getValue(enu,key));
  }
}

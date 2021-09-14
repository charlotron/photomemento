import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'sort'})
export class SortPipe implements PipeTransform {
  transform(array: any, field?: string, descending?: boolean): any[] {
    if (!Array.isArray(array)) return;
    array.sort((a: any, b: any) => {
      let a_val = field && a[field];
      let b_val = field && b[field];
      let descFactor: number = descending && -1 || 1;
      return descFactor *
        (a_val < b_val ?
          -1 :
          a_val > b_val ? //NOSONAR
            1 : 0);
    });
    return array;
  }
}

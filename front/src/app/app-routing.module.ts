import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./components/body/home/home.component";
import {DirsComponent} from "./components/body/dirs/dirs.component";
import {ManageComponent} from "./components/body/manage/manage.component";
import {StatsComponent} from "./components/body/stats/stats.component";
import {MapComponent} from "./components/body/map/map.component";

const routes: Routes = [{
  path: '',
  component: HomeComponent,
  data: {aliases: {normal: 'Home', short: '/'}, icon: 'home'},
  children: [
    {
      path: "prv/:id",
      component: HomeComponent
    }
  ]
}, {
  path: 'dirs',
  component: DirsComponent,
  data: {defaultChild: "root", aliases: {normal: 'Directories', short: 'Dirs'}, icon: 'folder'},
  children: [
    {
      path: ":id",
      component: DirsComponent,
      children: [
        {
          path: "prv/:id",
          component: DirsComponent
        }
      ]
    }
  ]
}, {
  path: 'map',
  component: MapComponent,
  data: {aliases: {normal: 'Map', short: 'Map'}, icon: 'map'}
}, {
  path: 'manage',
  component: ManageComponent,
  data: {aliases: {normal: 'Manage', short: 'Manage'}, classes: "mt-auto", icon: 'tools'}
},/* {
  path: 'config',
  component: ConfigComponent,
  data: {aliases: {normal: 'Config', short: 'Config'}, icon: 'cog'}
},*/ {
  path: 'stats',
  component: StatsComponent,
  data: {aliases: {normal: 'Stats', short: 'Stats'}, icon: 'chart-bar'}
}];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

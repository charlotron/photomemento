import {BrowserModule, HammerModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HeaderComponent} from './components/header/header.component';

import {HomeComponent} from './components/body/home/home.component';
import {HttpClientModule} from "@angular/common/http";
import {BodyComponent} from './components/body/body.component';
import {DirsComponent} from './components/body/dirs/dirs.component';
import {
  BreadcrumbModule,
  ButtonsModule,
  DropdownModule, IconsModule,
  ModalModule,
  NavbarModule,
  WavesModule
} from "angular-bootstrap-md";
import {NavbarComponent} from './components/header/navbar/navbar.component';
import {BreadcrumbsComponent} from "./components/flex/breadcrumbs/breadcrumbs.component";
import {SidebarComponent} from './components/sidebar/sidebar.component';
import {LinksComponent} from './components/flex/links/links.component';
import {MediaComponent} from './components/flex/gallery/media/media.component';
import {GalleryComponent} from './components/flex/gallery/gallery.component';
import {StopPropagationDirective} from './directives/stop-propagation.directive';
import {LoadedDirective} from './directives/loaded.directive';
import {DatePipe} from "@angular/common";
import {SortPipe} from './pipes/sort.pipe';
import {SublinkComponent} from './components/flex/links/sublink/sublink.component';
import {ConfigComponent} from './components/body/config/config.component';
import {ManageComponent} from './components/body/manage/manage.component';
import {MessagesComponent} from './components/flex/messages/messages.component';
import {MessageComponent} from './components/flex/messages/message/message.component';
import {StatsComponent} from './components/body/stats/stats.component';
import {ConfirmComponent} from './components/flex/confirm/confirm.component';
import {MapComponent} from './components/body/map/map.component';
import {FormsModule} from "@angular/forms";


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HomeComponent,
    BodyComponent,
    DirsComponent,
    NavbarComponent,
    BreadcrumbsComponent,
    SidebarComponent,
    LinksComponent,
    MediaComponent,
    GalleryComponent,
    StopPropagationDirective,
    LoadedDirective,
    SortPipe,
    SublinkComponent,
    ConfigComponent,
    ManageComponent,
    MessagesComponent,
    MessageComponent,
    StatsComponent,
    ConfirmComponent,
    MapComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    NavbarModule,
    BreadcrumbModule,
    WavesModule,
    DropdownModule.forRoot(),
    ModalModule,
    ButtonsModule,
    IconsModule,
    HammerModule,
    FormsModule
  ],
  providers: [DatePipe],
  bootstrap: [AppComponent]
})
export class AppModule {
}

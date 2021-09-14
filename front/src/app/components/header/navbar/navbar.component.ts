import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Params, Router} from "@angular/router";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  searchTerm: string;
  @ViewChild("searchInput") searchInput;

  constructor(
    private route: ActivatedRoute,
    private router: Router) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(async (params: Params) => {
      this.searchTerm = params.q;
    });
  }

  onSubmit(event: Event, f: HTMLFormElement) {
    event.stopPropagation();
    this.router.navigate(['/'], {queryParams: {q: f.q.value}});
    f.q.value="";
  }
}

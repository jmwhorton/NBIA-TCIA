import { Component, ViewEncapsulation, ViewChild, OnInit } from '@angular/core';
//import { TabViewModule } from 'primeng/tabview';
import { Config } from '../configs/config';
import { ConfigService } from '../configs/configservice';
import { Globals } from '../conf/globals'
import {TabView, TabPanel} from 'primeng/tabview';
import { GroupComponent } from '../group/group.component';
import { PeComponent } from '../pe/pe.component';
import { PgComponent } from '../pg/pg.component';
import { LoadingDisplayService } from '../common/components/loading-display/loading-display.service';

@Component({
  selector: 'nbia-uat',
  templateUrl: './nbia-uat.component.html',
  styleUrls: ['./nbia-uat.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class NbiaUatComponent implements OnInit {
	private addedUser: any;
	config: Config[];
	errorMessage: string;
	show: boolean;
	selectedTabHeader = "User";
	@ViewChild(TabView,{static:false}) tabView: TabView;
	@ViewChild(GroupComponent,{static:false}) groupComponent: GroupComponent;
	@ViewChild(PeComponent,{static:false}) peComponent: PeComponent;	
	@ViewChild(PgComponent,{static:false}) pgComponent: PgComponent;
	
	constructor(private appservice: ConfigService, private globals: Globals, private loadingDisplayService: LoadingDisplayService) { 	
		//uncomment below when check-in!!!
		//this.globals.serviceUrl = window.location.protocol +"//"+ window.location.host+"/nbia-api/services/v3/";
		this.globals.accessToken = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&')[0].split('=')[1]; 
//		this.loadingDisplayService.setLoading( true, 'Standby...' );
		this.appservice.getWikiUrlParam().then(data => {this.globals.wikiBaseUrl = data},
		error =>  {this.handleError(error);this.errorMessage = <any>error});		
	}

	ngOnInit() {
		this.appservice.getConfigParams().subscribe((config:Config[]) => {this.config = config; 
		this.show=this.config[1].paramValue.toLowerCase() == 'true';
		},
		error =>  {this.handleError(error);this.errorMessage = <any>error});

	}

	private pushNewUser(loginName) {
		this.addedUser = loginName;
	}
  
	private handleError (error: any) {
		let errMsg = error.message || 'Server error';
		console.error(errMsg); // log to console instead
		return Promise.reject(errMsg);
	}

	handleChange(e) {
		//this.selectedTabIndex = e.index;
		console.log("selected tab index="+ e.index);
		//console.log(this.tabView.tabs[this.selectedTabIndex].header);
		this.selectedTabHeader = this.tabView.tabs[e.index].header;
		console.log(this.selectedTabHeader);
		if (this.tabView.tabs[e.index].header == "Protection Element" && this.peComponent.loadingComplete == false)
			this.loadingDisplayService.setLoading( true, 'Loading existing collection and site data...' );
		
		if (this.tabView.tabs[e.index].header == "Protection Group" && this.pgComponent.loadingComplete == false)
			this.loadingDisplayService.setLoading( true, 'Loading protection group data...' );
		
		if (this.tabView.tabs[e.index].header == "User Group" && this.groupComponent.loadingComplete == false)
			this.loadingDisplayService.setLoading( true, 'Loading user group data...' );		
	}
}
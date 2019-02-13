import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonService } from '../../../../services/common.service';
import { ApiServerService } from '../../../../services/api-server.service';
import { ParameterService } from '@app/common/services/parameter.service';
import { InitMonitorService } from '@app/common/services/init-monitor.service';
import { QueryUrlService } from '@app/image-search/query-url/query-url.service';
import { Consts } from '@app/consts';

import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component( {
    selector: 'nbia-minimum-matched-studies',
    templateUrl: './minimum-matched-studies.component.html',
    styleUrls: ['../simple-search.component.scss', './minimum-matched-studies.component.scss']
} )
export class MinimumMatchedStudiesComponent implements OnInit, OnDestroy{

    /**
     * Used in code, and HTML.  Default start value is one.
     *
     * @type {number}
     */
    minNumberOfPoints = 1;

    toolTipText = 'Search results will show only subjects with at least this many studies (time points).';

    noQuery = true;

    private ngUnsubscribe: Subject<boolean> = new Subject<boolean>();

    constructor( private commonService: CommonService, private apiService: ApiServerService,
                 private parameterService: ParameterService, private initMonitorService: InitMonitorService,
                 private queryUrlService: QueryUrlService, private apiServerService: ApiServerService ) {


        // Set starting value.
        this.commonService.setMinimumMatchedStudiesValue( this.minNumberOfPoints );


    }

    ngOnInit() {

        this.onChangeMinimumMatchedStudies(); // Default: minNumberOfPoints = 1

        // Used when the Clear button is clicked in the Display Query
        this.commonService.resetAllSimpleSearchEmitter.pipe(takeUntil(this.ngUnsubscribe)).subscribe(
            () => {
                this.minNumberOfPoints = 1;
                this.onChangeMinimumMatchedStudies( false );
                this.commonService.emitSimpleSearchQueryForDisplay( [] );
            }
        );


        this.parameterService.parameterMinimumStudiesEmitter.pipe(takeUntil(this.ngUnsubscribe)).subscribe(
            data => {
                this.minNumberOfPoints = +data;
                this.commonService.setMinimumMatchedStudiesValue( this.minNumberOfPoints );
                this.commonService.setHaveUserInput( false );

            }
        );


        this.commonService.searchResultsCountEmitter.pipe(takeUntil(this.ngUnsubscribe)).subscribe(
            data => {
                if( data === -1 ){ // disable
                    this.noQuery = true;
                    this.minNumberOfPoints = 1;
                }
                else{
                    this.noQuery = false;
                }
            }
        );


        // This will tell the parameter service that it can now send us any query criteria that where passed in the URL
        this.initMonitorService.setMinimumStudiesInit( true );

    }

    /**
     * Called when the value of the UI "Number spinner" changes.
     */
    onChangeMinimumMatchedStudies( runQuery = true ) {
        if( this.minNumberOfPoints < 1 ){
            this.minNumberOfPoints = 1;
        }
        // If this method was called from a URL parameter search, setHaveUserInput will be set to false,
        // this method is by user action only, so set setHaveUserInput to true.
        this.commonService.setHaveUserInput( true );

        this.commonService.setMinimumMatchedStudiesValue( this.minNumberOfPoints );
        this.queryUrlService.update( this.queryUrlService.MIN_STUDIES, this.minNumberOfPoints );

        //////////////////////////////////////////////////

        this.apiServerService.getCriteriaCounts();

        //////////////////////////////////////////////////
        let criteriaForQuery: string[] = [];
        criteriaForQuery.push( Consts.MINIMUM_STUDIES );
        criteriaForQuery.push( this.minNumberOfPoints.toString() );
        if( runQuery ){
            this.commonService.updateQuery( criteriaForQuery );
        }
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}

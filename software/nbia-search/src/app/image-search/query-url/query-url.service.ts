import { Injectable } from '@angular/core';
import { UtilService } from '@app/common/services/util.service';
import { Properties } from '@assets/properties';
import { Consts } from '@app/consts';

@Injectable({
  providedIn: 'root'
})
export class QueryUrlService{

    IMAGE_MODALITY = 1;
    IMAGE_MODALITY_ALL = 2;
    ANATOMICAL_SITE = 3;
    MIN_STUDIES = 4;
    DATE_RANGE = 5;
    SUBJECT_ID = 6;
    COLLECTIONS = 7;
    SPECIES = 8;
    PHANTOMS = 9;
    THIRD_PARTY = 10;
    DAYS_FROM_BASELINE = 11;
    EXCLUDE_COMMERCIAL = 12;
    API_URL = 0;

    queryUrlString = '';

    queryData = [];

    constructor( private utilService: UtilService ) {
    }

    update( criteriaType, value ) {
        this.queryData[criteriaType] = value;
    }

    clear( criteriaType ) {
        this.queryData[criteriaType] = null;
    }

    getQueryForLogging(){
        let queryForLoggingObject = {};

        for( let f = 0; f < this.queryData.length; f++ ){
            if( !this.utilService.isNullOrUndefined( this.queryData[f] ) ){
                switch( f ){

/*
                    case this.API_URL:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Properties.URL_KEY_API_URL])){
                            queryForLoggingObject[Properties.URL_KEY_API_URL] = [];
                        }
                        queryForLoggingObject[Properties.URL_KEY_API_URL].push(this.queryData[f]);
                        break;
*/
                    case this.COLLECTIONS:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Consts.COLLECTION])){
                            queryForLoggingObject[Consts.COLLECTION] = [];
                        }
                        queryForLoggingObject[Consts.COLLECTION].push(this.queryData[f]);
                        break;

                    case this.IMAGE_MODALITY:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Consts.IMAGE_MODALITY])){
                            queryForLoggingObject[Consts.IMAGE_MODALITY] = [];
                        }
                        queryForLoggingObject[Consts.IMAGE_MODALITY].push(this.queryData[f]);

                        break;

                    case this.IMAGE_MODALITY_ALL:
                        if( this.queryData[f] === 0 ){
                            if( this.utilService.isNullOrUndefined( queryForLoggingObject[Properties.URL_KEY_MODALITY_ALL])){
                                queryForLoggingObject[Properties.URL_KEY_MODALITY_ALL] = [];
                            }
                            queryForLoggingObject[Properties.URL_KEY_MODALITY_ALL].push(true);
                        }
                        break;

                    case this.ANATOMICAL_SITE:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Consts.ANATOMICAL_SITE])){
                            queryForLoggingObject[Consts.ANATOMICAL_SITE] = [];
                        }
                        queryForLoggingObject[Consts.ANATOMICAL_SITE].push(this.queryData[f]);
                        break;

                    case this.SPECIES:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Consts.SPECIES])){
                            queryForLoggingObject[Consts.SPECIES] = [];
                        }
                        queryForLoggingObject[Consts.SPECIES].push(this.queryData[f]);
                        break;

                    case this.PHANTOMS:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Consts.PHANTOMS_CRITERIA])){
                            queryForLoggingObject[Consts.PHANTOMS_CRITERIA] = [];
                        }
                        queryForLoggingObject[Consts.PHANTOMS_CRITERIA].push(this.queryData[f]);
                        break;

                    case this.THIRD_PARTY:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Consts.THIRD_PARTY_CRITERIA])){
                            queryForLoggingObject[Consts.THIRD_PARTY_CRITERIA] = [];
                        }
                        queryForLoggingObject[Consts.THIRD_PARTY_CRITERIA].push(this.queryData[f]);
                        break;

                    case this.MIN_STUDIES:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Properties.URL_KEY_MINIMUM_STUDIES])){
                            queryForLoggingObject[Properties.URL_KEY_MINIMUM_STUDIES] = [];
                        }
                        queryForLoggingObject[Properties.URL_KEY_MINIMUM_STUDIES].push(this.queryData[f]);

                        break;

                    case this.DATE_RANGE:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Properties.URL_KEY_DATE_RANGE])){
                            queryForLoggingObject[Properties.URL_KEY_DATE_RANGE] = [];
                        }
                        queryForLoggingObject[Properties.URL_KEY_DATE_RANGE].push(this.queryData[f]);

                        break;

                    case this.SUBJECT_ID:
                        if( this.utilService.isNullOrUndefined( queryForLoggingObject[Properties.URL_KEY_PATIENT_ID])){
                            queryForLoggingObject[Properties.URL_KEY_PATIENT_ID] = [];
                        }
                        queryForLoggingObject[Properties.URL_KEY_PATIENT_ID].push(this.queryData[f]);

                        break;
                }
            }
        }
        return queryForLoggingObject;
    }


    getQueryUrl() {
        this.queryUrlString = '';
        let queryUrlString = '';
        for( let f = 0; f < this.queryData.length; f++ ){

            if( !this.utilService.isNullOrUndefined( this.queryData[f] ) ){
                switch( f ){
                    case this.DAYS_FROM_BASELINE:
                        this.queryUrlString += '&' + Properties.URL_KEY_DAYS_FROM_BASELINE + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_DAYS_FROM_BASELINE + '=' + this.queryData[f];

                        break;
                    case this.EXCLUDE_COMMERCIAL:
                        this.queryUrlString += '&' + Properties.URL_KEY_EXCLUDE_COMMERCIAL + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_EXCLUDE_COMMERCIAL + '=' + this.queryData[f];
                        break;

                    case this.API_URL:
                        this.queryUrlString += '&' + Properties.URL_KEY_API_URL + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_API_URL + '=' + this.queryData[f];
                        break;

                    case this.COLLECTIONS:
                        this.queryUrlString += '&' + Properties.URL_KEY_COLLECTIONS + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_COLLECTIONS + '=' + this.queryData[f];
                        break;

                    case this.IMAGE_MODALITY:
                        this.queryUrlString += '&' + Properties.URL_KEY_MODALITY + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_MODALITY + '=' + this.queryData[f];
                        break;

                    case this.IMAGE_MODALITY_ALL:
                        if( this.queryData[f] === 0 ){
                            this.queryUrlString += '&' + Properties.URL_KEY_MODALITY_ALL + '=true';
                            queryUrlString += '&' + Properties.URL_KEY_MODALITY_ALL + '=true';
                        }
                        break;

                    case this.ANATOMICAL_SITE:
                        this.queryUrlString += '&' + Properties.URL_KEY_ANATOMICAL_SITE + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_ANATOMICAL_SITE + '=' + this.queryData[f];
                        break;

                    case this.SPECIES:
                        this.queryUrlString += '&' + Properties.URL_KEY_SPECIES + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_SPECIES + '=' + this.queryData[f];
                        break;

                    case this.PHANTOMS:
                        this.queryUrlString += '&' + Properties.URL_KEY_PHANTOMS + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_PHANTOMS + '=' + this.queryData[f];
                        break;

                    case this.THIRD_PARTY:
                        this.queryUrlString += '&' + Properties.URL_KEY_THIRD_PARTY + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_THIRD_PARTY + '=' + this.queryData[f];
                        break;

                    case this.MIN_STUDIES:
                        this.queryUrlString += '&' + Properties.URL_KEY_MINIMUM_STUDIES + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_MINIMUM_STUDIES + '=' + this.queryData[f];
                        break;

                    case this.DATE_RANGE:
                        this.queryUrlString += '&' + Properties.URL_KEY_DATE_RANGE + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_DATE_RANGE + '=' + this.queryData[f];
                        break;

                    case this.SUBJECT_ID:
                        this.queryUrlString += '&' + Properties.URL_KEY_PATIENT_ID + '=' + this.queryData[f];
                        queryUrlString += '&' + Properties.URL_KEY_PATIENT_ID + '=' + this.queryData[f];
                        break;
                }
            }
        }

        this.queryUrlString = location.href.toString().replace( /\?.*/, '') + '?' +  this.queryUrlString.substr( 1 );
        queryUrlString = location.href.toString().replace( /\?.*/, '') + '?' +  queryUrlString.substr( 1 );
        return queryUrlString;
    }
}

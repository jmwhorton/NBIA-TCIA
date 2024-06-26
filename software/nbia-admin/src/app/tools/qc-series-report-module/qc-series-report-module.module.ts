import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { QcSeriesReportTableComponent } from './qc-series-report-table/qc-series-report-table.component';
import { FormsModule } from '@angular/forms';
import { QcSeriesReportComponent } from './qc-series-report/qc-series-report.component';



@NgModule( {
    declarations: [QcSeriesReportTableComponent, QcSeriesReportComponent],
    exports: [
        QcSeriesReportTableComponent,
        QcSeriesReportComponent
    ],
    imports: [
        FormsModule,
        CommonModule
    ]
})
export class QcSeriesReportModuleModule { }

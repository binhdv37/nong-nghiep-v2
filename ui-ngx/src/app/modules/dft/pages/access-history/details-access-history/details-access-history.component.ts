import {Component, Inject, OnInit} from '@angular/core';
import {AuditLog} from "@shared/models/audit-log.models";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {AccessHistoryService} from "@modules/dft/service/accessHistory/access-history.service";

export interface DialogData {
  id: string;
}

@Component({
  selector: 'tb-details-access-history',
  templateUrl: './details-access-history.component.html',
  styleUrls: ['./details-access-history.component.scss']
})
export class DetailsAccessHistoryComponent implements OnInit {

  currentLog: AuditLog;

  constructor(private accessHistoryService: AccessHistoryService,
              private dialogRef: MatDialogRef<DetailsAccessHistoryComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  ngOnInit(): void {
    this.accessHistoryService.getAccessHistory(this.data.id).subscribe(
      next => {
        this.currentLog = next;
        console.log(this.currentLog);
      }, error => {
        console.log(error);
        this.currentLog = null;
      }
    );
  }

}

import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ConfirmDialogData} from '@shared/components/dialog/confirm-dialog.component';

@Component({
  selector: 'tb-confirm-save-diaglog',
  templateUrl: './confirm-save-diaglog.component.html',
  styleUrls: ['./confirm-save-diaglog.component.scss']
})
export class ConfirmSaveDiaglogComponent {

  constructor(public dialogRef: MatDialogRef<ConfirmSaveDiaglogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}
}

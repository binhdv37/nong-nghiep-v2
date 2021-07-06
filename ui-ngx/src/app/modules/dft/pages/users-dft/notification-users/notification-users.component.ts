import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

export interface DialogData {
  action: string;
  message: string;
}

@Component({
  selector: 'tb-notification-users',
  templateUrl: './notification-users.component.html',
  styleUrls: ['./notification-users.component.scss']
})
export class NotificationUsersComponent implements OnInit {

  constructor(private dialogRef: MatDialogRef<NotificationUsersComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  ngOnInit(): void {
  }

}

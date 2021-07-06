import {Component, Input, Output, EventEmitter} from '@angular/core';
import {InitPermission} from '@modules/dft/pages/role/edit-role/edit-role.component';

@Component({
  selector: 'binhdv-check-box-tree',
  templateUrl: './check-box-tree.component.html',
  styleUrls: ['./check-box-tree.component.scss']
})
export class CheckBoxTreeComponent  {
  /*
     component này nhận vào 1 InitPermision object, hiển thị lên màn hình dạng checkbox tree,
     emit checkbox value mỗi khi checkbox.checked có sự thay đổi.
  */

  // xem chi tiet => disable all checkbox
  @Input() isDetailEntity: boolean;

  // danh sach quyen nhan vao
  @Input() permission: InitPermission;

  // permission's name emitter
  @Output() quyenEmitter = new EventEmitter<string>();

  // user click node cha => emit node cha value || emit cac gia tri node con ma co su thay doi truong completed
  setAll(completed: boolean) {
    this.permission.completed = completed;
    // k có node con => emit node cha value
    if (this.permission.child == null) {
      this.emitData(this.permission.name);
      return;
    }

    // emit các node con có sự thay đổi checked
    this.permission.child.forEach(x => {
      if (x.completed !== completed){
        x.completed = completed;
        this.emitData(x.name);
      }
    });
  }

  updateAllComplete(name: string) {
    // emit data
    this.emitData(name);

    // update all completed
    this.permission.completed = this.permission.child != null &&
      this.permission.child.every(t => t.completed);
  }

  someComplete(): boolean {
    if (this.permission.child == null) {
      return false;
    }
    return this.permission.child.filter(t => t.completed).length > 0 && !this.permission.completed;
  }

  // emit quyen to parent component
  emitData(data: string){
    this.quyenEmitter.emit(data);
  }


}

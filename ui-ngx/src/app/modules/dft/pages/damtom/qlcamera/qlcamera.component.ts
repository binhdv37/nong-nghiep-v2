import {ChangeDetectorRef, Component, ElementRef, HostListener, Input, OnInit, ViewChild, AfterViewInit} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {merge, Subject, Subscription} from 'rxjs';
import {MatTableDataSource} from '@angular/material/table';
import {PageLink} from '@shared/models/page/page-link';
import {Direction, SortOrder} from '@shared/models/page/sort-order';
import {TableAction} from '@modules/dft/models/action.model';
import {Store} from '@ngrx/store';
import {AppState} from '@core/core.state';
import {ActivatedRoute} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {MatDialog} from '@angular/material/dialog';
import {DialogService} from '@core/services/dialog.service';
import {ToastrService} from 'ngx-toastr';
import {DomSanitizer} from '@angular/platform-browser';
import {CameraService} from '@modules/dft/service/qlcamera/camera.service';
import {catchError, finalize, tap} from 'rxjs/operators';
import {PageData} from '@shared/models/page/page-data';
import {DamtomCamera} from '@modules/dft/models/qlcamera.model';
import {EditCameraComponent} from '@modules/dft/pages/damtom/qlcamera/edit-camera/edit-camera.component';
import {escapedHTML} from '@modules/dft/service/utils.service';
import {DftAdminSettingsService} from '@modules/dft/service/khachhang/camera.service';

@Component({
  selector: 'tb-qlcamera',
  templateUrl: './qlcamera.component.html',
  styleUrls: ['./qlcamera.component.scss']
})
export class QlcameraComponent implements OnInit, AfterViewInit {
  @Input() damtomId: string;

  @ViewChild('searchInput') searchInput: ElementRef;

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild('sort', { static: true }) sort: MatSort;

  @ViewChild('leftcam') leftcam: ElementRef;

  // @ViewChild('mainVideo') mainVideo: ElementRef;

  isLoading$: Subject<boolean>;

  dataSource = new MatTableDataSource<DamtomCamera>();
  displayedColumns: string[] = ['actions', 'code', 'name', 'note'];
  defaultPageSize = 10;
  // displayPagination = true;
  pageSizeOptions = [10, 20, 30, 40, 50, 100];
  pageLink: PageLink;
  sortOrder: SortOrder;

  totalPages: number;
  totalElements: number;

  listCameras: DamtomCamera[] = [];
  mainCamera: DamtomCamera;

  isFullscreen: boolean = false;

  cameraServerUrl = '';

  // listen to fullscreen change event
  @HostListener('document:fullscreenchange', ['$event'])
  handleKeydownEvent(event: KeyboardEvent) {
    this.isFullscreen = !this.isFullscreen;
  }

  public get tableAction(): typeof TableAction {
    return TableAction;
  }

  private searchEmitter$ = new Subject();
  private subscriptions$: Subscription[] = [];

  constructor(protected store: Store<AppState>,
              public route: ActivatedRoute,
              public translate: TranslateService,
              public dialog: MatDialog,
              private dialogService: DialogService,
              private toastrService: ToastrService,
              private domSanitizer: DomSanitizer,
              private dftAdminSettingsService: DftAdminSettingsService,
              private cd: ChangeDetectorRef,
              private cameraService: CameraService) {
    this.isLoading$ = new Subject<boolean>();
  }

  ngOnInit() {
    this.getCameraUrl();
    this.sortOrder = {
      property: 'createdTime',
      direction: Direction.DESC
    },
      this.pageLink = new PageLink(this.defaultPageSize, 0, '', this.sortOrder);
    this.initData();
  }

  ngOnDestroy() {
    this.isLoading$.next(false);
  }

  ngAfterViewInit(): void {
    $( '.mat-paginator-range-actions' ).append( '<div class="new-div"></div>' );
    $('.mat-paginator-range-actions >button').appendTo( $('.new-div') );
    const sortSubscription = this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    this.subscriptions$.push(sortSubscription);

    const paginatorSubscriptions = merge(this.sort.sortChange, this.paginator.page).pipe(
      tap(() => this.getData()),
    ).subscribe();
    this.subscriptions$.push(paginatorSubscriptions);
  }

  initData() {
    this.fetchData(true);
    this.searchEmitter$.next();
  }

  fetchData(isUpdated: boolean) {
    this.isLoading$.next(true);
    this.dataSource.data = [];
    this.cameraService
      .getDamtomCameras(this.damtomId, this.pageLink).pipe(
      tap((pageData: PageData<DamtomCamera>) => {
        if (pageData.data.length > 0) {

          // neu la them, sua, xoa camera => change list cameras
          if (isUpdated){
            this.getAllCameras();
          }

          this.dataSource.data = pageData.data;

          this.totalPages = pageData.totalPages;
          this.totalElements = pageData.totalElements;
        } else {
          if (isUpdated){
            this.getAllCameras();
          }

          this.dataSource.data = [];
        }
      }),
      finalize(() => {
        this.isLoading$.next(false);
      }),
      catchError((error) => {
        console.log(error);
        return null;
      })
    ).subscribe();
  }

  getAllCameras(){
    this.isLoading$.next(true);
    this.cameraService.getAllDamtomCameras(this.damtomId).subscribe(
      cameras => {
        this.listCameras = cameras;
        this.toggleCamera(this.findMainCamera(this.listCameras));
      },
      error => {
        console.log(error);
      },
      () => {
        this.isLoading$.next(false);
      }
    );
  }

  getCameraUrl(){
    this.dftAdminSettingsService.getCameraServerUrl().subscribe(
      resp => {
        this.cameraServerUrl = resp;
      },
      err => {
        console.log(err);
      },
      () => {
        console.log('cameraServerurl : ' + this.cameraServerUrl);
      }
    );
  }

  onSearch(){
    this.paginator.pageIndex = 0;
    this.getData();
  }

  getData() {
    this.initQueryFind();
    this.fetchData(false);
    this.searchEmitter$.next();
  }

  deleteEntity(id: string, name: string) {
    this.dialogService.confirm(
      this.translate.instant('dft.qlcamera.delete-camera-title'),
      this.translate.instant('Camera ' + escapedHTML(name) + ' sẽ bị xóa!'),
      this.translate.instant('dft.qlcamera.cancel-button'),
      this.translate.instant('dft.qlcamera.delete-button'),
      true
    ).subscribe((result) => {
      if (result) {
        this.isLoading$.next(true);
        this.cameraService
          .deleteCameraById(id).pipe(
          tap((data) => {
            if (data === 1){
              this.toastrService.success(this.translate.instant('dft.qlcamera.notify.delete-success'), '', {
                positionClass: 'toast-bottom-right',
                timeOut: 3000,
              });
            }
            this.refresh();
          }),
          finalize(() => {
            this.isLoading$.next(false);
          }),
          catchError((error) => {
            console.log(error);
            return null;
          })
        ).subscribe();
      }
    });
  }

  initQueryFind() {
    this.sortOrder = {
      property: this.sort.active != undefined ? this.sort.active : 'createdTime',
      direction: this.sort.direction != "" ? Direction[this.sort.direction.toUpperCase()] : Direction.DESC
    },
      this.pageLink = new PageLink(
        this.paginator.pageSize,
        this.paginator.pageIndex,
        this.searchInput.nativeElement.value,
        this.sortOrder);
  }

  refresh() {
    this.searchInput.nativeElement.value = '';
    this.ngOnInit();
  }

  // update default page size
  public handlePage(e: any) {
    this.defaultPageSize = e.pageSize;
  }

  openEditDialog(cameraId: string, name: string, actionOpen: string): void {
    const dialogRef = this.dialog.open(EditCameraComponent, {
      data: {damtomId: this.damtomId, id: cameraId, name, action: actionOpen }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === TableAction.ADD_ENTITY) { // Thêm mới thành công
        this.toastrService.success(this.translate.instant('dft.qlcamera.notify.add-success'), '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.refresh();
      } else if (result === TableAction.EDIT_ENTITY){
        this.toastrService.success(this.translate.instant('dft.qlcamera.notify.edit-success'), '', {
          positionClass: 'toast-bottom-right',
          timeOut: 3000,
        });
        this.refresh();
      }
    });
  }

  // find main camera to display in left side :
  // main camera is camera with main == true || camera dau danh sach(danh sach mac dinh sap xep theo createdTime desc)
  findMainCamera(listCameras: DamtomCamera[]){
    if (listCameras.length === 0) { return null; }
    for (const camera of listCameras){
      if (camera.main) { return camera; }
    }
    return listCameras[0];
  }

  // toggle main camera
  toggleCamera(camera: DamtomCamera){
    this.mainCamera = camera;
  }

  // redirect to shinobi
  openShinobi(){
    if (!this.cameraServerUrl.startsWith('http') && !this.cameraServerUrl.startsWith('https')){
      window.open(`http://${this.cameraServerUrl}`, '_blank');
    }
    else {
      window.open(this.cameraServerUrl, 'blank');
    }
  }

  // full screen
  openFullscreen(){
    if (this.leftcam.nativeElement.requestFullscreen) {
      this.leftcam.nativeElement.requestFullscreen();
    } else if (this.leftcam.nativeElement.webkitRequestFullscreen) { /* Safari */
      this.leftcam.nativeElement.webkitRequestFullscreen();
    } else if (this.leftcam.nativeElement.msRequestFullscreen) { /* IE11 */
      this.leftcam.nativeElement.msRequestFullscreen();
    }
  }

  // close full screen
  closeFullscreen() {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    }
    // th safari, ie ?
  }

}

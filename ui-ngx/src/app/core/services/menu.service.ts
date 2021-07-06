///
/// Copyright © 2016-2021 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///
import {Injectable} from '@angular/core';
import {HomeSection, MenuSection} from '@core/services/menu.models';
import {guid} from '@core/utils';
import {select, Store} from '@ngrx/store';
import {Authority} from '@shared/models/authority.enum';
import {AuthUser} from '@shared/models/user.model';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {take} from 'rxjs/operators';

import {selectAuthUser, selectIsAuthenticated} from '../auth/auth.selectors';
import {AuthService} from '../auth/auth.service';
import {AppState} from '../core.state';


// binhdv
export interface MenuHelper {
  permissions: string[];
  menuSection: MenuSection; // menu section này đc build khi user có tất cả các permissions bên trên
}


@Injectable({
  providedIn: 'root'
})
export class MenuService {

  constructor(private store: Store<AppState>, private authService: AuthService) {
    this.store.pipe(select(selectIsAuthenticated)).subscribe(
      (authenticated: boolean) => {
        if (authenticated) {
          this.buildMenu();
        }
      }
    );
  }

  // binhdv
  myMenuHelpers: MenuHelper[] = [
    {
      permissions: [Authority.PAGES_USERS],
      menuSection: {
        id: guid(),
        name: 'dft.users.manage',
        type: 'link',
        path: '/account',
        icon: 'UsersMenu'
      }
    },
    {
      permissions: [Authority.PAGES_ROLES],
      menuSection: {
        id: guid(),
        name: 'dft.role.type',
        type: 'link',
        path: '/role',
        icon: 'ShieldCheckMenu'
      }
    },
    {
      permissions: [Authority.PAGES_DAMTOM],
      menuSection: {
        id: guid(),
        name: 'dft.damtom.type',
        type: 'link',
        path: '/dam-tom',
        icon: 'MapPinMenu'
      }
    },
    {
      permissions: [Authority.PAGES_USERS_ACCESS_HISTORY],
      menuSection: {
        id: guid(),
        name: 'dft.users.access-history',
        type: 'link',
        path: '/logs',
        icon: 'ClockCounterClockwiseMenu'
      }
    },
    {
      permissions: [Authority.PAGES_DATLICH_XUATBC],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.lap-lich-bc',
        type: 'link',
        path: '/report-schedule',
        icon: 'CalendarBlankMenu'
      }
    },
    {
      permissions: [Authority.PAGES_DAMTOM],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.bc-dashboard',
        type: 'link',
        path: '/damtom-dashboard',
        icon: 'PresentationChart'
      }
    },
  ];

  // binhdv - bao cao menu secion
  baocaoMenuHelpers: MenuHelper[] = [
    {
      permissions: [Authority.PAGES_BC_TONGHOP],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.bc-tong-hop',
        type: 'link',
        path: '/bao-cao/bc-tonghop',
        icon: 'bcTongHopMenu'
      }
    },
    {
      permissions: [Authority.PAGES_BC_DLGIAMSAT],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.bc-dl-giamsat',
        type: 'link',
        path: '/bao-cao/bc-dlgiamsat',
        icon: 'bcDuLieuGiamSatMenu'
      }
    },
    {
      permissions: [Authority.PAGES_BC_CANHBAO],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.bc-canh-bao',
        type: 'link',
        path: '/bao-cao/bc-canhbao',
        icon: 'bcCanhBaoMenu'
      }
    },
    {
      permissions: [Authority.PAGES_BC_KETNOI_CAMBIEN],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.bc-ketnoi-cb',
        type: 'link',
        path: '/bao-cao/bc-ketnoi-cambien',
        icon: 'bcCamBienMenu'
      }
    },
    {
      permissions: [Authority.PAGES_BC_GUI_TTCB],
      menuSection: {
        id: guid(),
        name: 'dft.bao-cao.bc-guitt-cb',
        type: 'link',
        path: '/bao-cao/bc-gui-ttcb',
        icon: 'bcThongBaoMenu'
      }
    }
  ];


  menuSections$: Subject<Array<MenuSection>> = new BehaviorSubject<Array<MenuSection>>([]);
  homeSections$: Subject<Array<HomeSection>> = new BehaviorSubject<Array<HomeSection>>([]);

  private buildMenu() {
    this.store.pipe(select(selectAuthUser), take(1)).subscribe(
      (authUser: AuthUser) => {
        if (authUser) {
          // binhdv
          console.log(authUser);
          let menuSections: Array<MenuSection>;
          let homeSections: Array<HomeSection>;
          switch (authUser.authority) {
            case Authority.SYS_ADMIN:
              menuSections = this.buildSysAdminMenu(authUser);
              homeSections = this.buildSysAdminHome(authUser);
              break;
            case Authority.TENANT_ADMIN:
              menuSections = this.buildTenantAdminMenu(authUser);
              homeSections = this.buildTenantAdminHome(authUser);
              break;
            case Authority.CUSTOMER_USER:
              menuSections = this.buildCustomerUserMenu(authUser);
              homeSections = this.buildCustomerUserHome(authUser);
              break;
          }
          this.menuSections$.next(menuSections);
          this.homeSections$.next(homeSections);
        }
      }
    );
  }

  private buildSysAdminMenu(authUser: any): Array<MenuSection> {
    const sections: Array<MenuSection> = [];
    sections.push(
      {
        id: guid(),
        name: 'home.home',
        type: 'link',
        path: '/home',
        icon: 'HouseMenu'
      },
      {
        id: guid(),
        name: 'dft.admin.khachhang.manage',
        type: 'link',
        path: '/sys-admin/khach-hang',
        icon: 'UsersMenu'
      },
      // {
      //   id: guid(),
      //   name: 'tenant.tenants',
      //   type: 'link',
      //   path: '/tenants',
      //   icon: 'supervisor_account'
      // },
      // {
      //   id: guid(),
      //   name: 'tenant-profile.tenant-profiles',
      //   type: 'link',
      //   path: '/tenantProfiles',
      //   icon: 'mdi:alpha-t-box',
      //   isMdiIcon: true
      // },
      // {
      //   id: guid(),
      //   name: 'widget.widget-library',
      //   type: 'link',
      //   path: '/widgets-bundles',
      //   icon: 'now_widgets'
      // },
      {
        id: guid(),
        name: 'admin.system-settings',
        type: 'toggle',
        path: '/settings',
        height: '200px',
        icon: 'settings',
        pages: [
          {
            id: guid(),
            name: 'admin.general',
            type: 'link',
            path: '/settings/general',
            icon: 'settings_applications'
          },
          {
            id: guid(),
            name: 'admin.outgoing-mail',
            type: 'link',
            path: '/settings/outgoing-mail',
            icon: 'mail'
          },
          {
            id: guid(),
            name: 'dft.admin.camera.setting',
            type: 'link',
            path: '/settings/camera',
            icon: 'videocam'
          },
          {
            id: guid(),
            name: 'dft.admin.notification.setting',
            type: 'link',
            path: '/settings/notification',
            icon: 'notifications'
          },

          // {
          //   id: guid(),
          //   name: 'admin.sms-provider',
          //   type: 'link',
          //   path: '/settings/sms-provider',
          //   icon: 'sms'
          // },
          // {
          //   id: guid(),
          //   name: 'admin.security-settings',
          //   type: 'link',
          //   path: '/settings/security-settings',
          //   icon: 'security'
          // },
          // {
          //   id: guid(),
          //   name: 'admin.oauth2.oauth2',
          //   type: 'link',
          //   path: '/settings/oauth2',
          //   icon: 'security'
          // }
        ]
      },
    );
    return sections;
  }

  private buildSysAdminHome(authUser: any): Array<HomeSection> {
    const homeSections: Array<HomeSection> = [];
    homeSections.push(
      {
        name: 'dft.admin.khachhang.manage',
        places: [
          {
            name: 'dft.admin.khachhang.manage',
            icon: 'UsersMenu',
            path: '/sys-admin/khach-hang'
          }
        ]
      },
      // {
      //   name: 'tenant.management',
      //   places: [
      //     {
      //       name: 'tenant.tenants',
      //       icon: 'supervisor_account',
      //       path: '/tenants'
      //     },
      //     {
      //       name: 'tenant-profile.tenant-profiles',
      //       icon: 'mdi:alpha-t-box',
      //       isMdiIcon: true,
      //       path: '/tenantProfiles'
      //     },
      //   ]
      // },
      // {
      //   name: 'widget.management',
      //   places: [
      //     {
      //       name: 'widget.widget-library',
      //       icon: 'now_widgets',
      //       path: '/widgets-bundles'
      //     }
      //   ]
      // },
      {
        name: 'admin.system-settings',
        places: [
          {
            name: 'admin.general',
            icon: 'settings_applications',
            path: '/settings/general'
          },
          {
            name: 'admin.outgoing-mail',
            icon: 'mail',
            path: '/settings/outgoing-mail'
          },
          {
            name: 'dft.admin.camera.setting',
            icon: 'videocam',
            path: '/settings/camera'
          },
          {
            name: 'dft.admin.notification.setting',
            icon: 'notifications',
            path: '/settings/notification'
          },
          // {
          //   name: 'admin.sms-provider',
          //   icon: 'sms',
          //   path: '/settings/sms-provider'
          // },
          // {
          //   name: 'admin.security-settings',
          //   icon: 'security',
          //   path: '/settings/security-settings'
          // },
          // {
          //   name: 'admin.oauth2.oauth2',
          //   icon: 'security',
          //   path: '/settings/oauth2'
          // }
        ]
      }
    );
    return homeSections;
  }

  private buildTenantAdminMenu(authUser: any): Array<MenuSection> {
    const sections: Array<MenuSection> = [];
    sections.push(
      {
        id: guid(),
        name: 'home.home',
        type: 'link',
        path: '/home',
        icon: 'HouseMenu'
      },
      {
        id: guid(),
        name: 'dft.damtom.type',
        type: 'link',
        path: '/dam-tom',
        icon: 'MapPinMenu'
      },
      {
        id: guid(),
        name: 'dft.role.type',
        type: 'link',
        path: '/role',
        icon: 'ShieldCheckMenu'
      },
      {
        id: guid(),
        name: 'dft.users.manage',
        type: 'link',
        path: '/account',
        icon: 'UsersMenu'
      },
      {
        id: guid(),
        name: 'dft.users.access-history',
        type: 'link',
        path: '/logs',
        icon: 'ClockCounterClockwiseMenu'
      },
      {
        id: guid(),
        name: 'dft.bao-cao.lap-lich-bc',
        type: 'link',
        path: '/report-schedule',
        icon: 'CalendarBlankMenu'
      },
      {
        id: guid(),
        name: 'dft.bao-cao.bc-dashboard',
        type: 'link',
        path: '/damtom-dashboard',
        icon: 'PresentationChart'
      },
      {
        id: guid(),
        name: 'dft.bao-cao.bao-cao',
        type: 'toggle',
        path: '/bao-cao',
        height: '300px',
        icon: 'ClipboardTextMenu',
        pages: [
          {
            id: guid(),
            name: 'dft.bao-cao.bc-tong-hop',
            type: 'link',
            path: '/bao-cao/bc-tonghop',
            icon: 'bcTongHopMenu'
          },
          {
            id: guid(),
            name: 'dft.bao-cao.bc-dl-giamsat',
            type: 'link',
            path: '/bao-cao/bc-dlgiamsat',
            icon: 'bcDuLieuGiamSatMenu'
          },
          {
            id: guid(),
            name: 'dft.bao-cao.bc-canh-bao',
            type: 'link',
            path: '/bao-cao/bc-canhbao',
            icon: 'bcCanhBaoMenu'
          },
          {
            id: guid(),
            name: 'dft.bao-cao.bc-ketnoi-cb',
            type: 'link',
            path: '/bao-cao/bc-ketnoi-cambien',
            icon: 'bcCamBienMenu'
          },
          {
            id: guid(),
            name: 'dft.bao-cao.bc-guitt-cb',
            type: 'link',
            path: '/bao-cao/bc-gui-ttcb',
            icon: 'bcThongBaoMenu'
          }
        ]
      }
      // {
      //   id: guid(),
      //   name: 'rulechain.rulechains',
      //   type: 'link',
      //   path: '/ruleChains',
      //   icon: 'settings_ethernet'
      // },
      // {
      //   id: guid(),
      //   name: 'customer.customers',
      //   type: 'link',
      //   path: '/customers',
      //   icon: 'supervisor_account'
      // },
      // {
      //   id: guid(),
      //   name: 'asset.assets',
      //   type: 'link',
      //   path: '/assets',
      //   icon: 'domain'
      // },
      // {
      //   id: guid(),
      //   name: 'device.devices',
      //   type: 'link',
      //   path: '/devices',
      //   icon: 'devices_other'
      // },
      // {
      //   id: guid(),
      //   name: 'device-profile.device-profiles',
      //   type: 'link',
      //   path: '/deviceProfiles',
      //   icon: 'mdi:alpha-d-box',
      //   isMdiIcon: true
      // },
      // {
      //   id: guid(),
      //   name: 'entity-view.entity-views',
      //   type: 'link',
      //   path: '/entityViews',
      //   icon: 'view_quilt'
      // },
      // {
      //   id: guid(),
      //   name: 'widget.widget-library',
      //   type: 'link',
      //   path: '/widgets-bundles',
      //   icon: 'now_widgets'
      // },
      // {
      //   id: guid(),
      //   name: 'dashboard.dashboards',
      //   type: 'link',
      //   path: '/dashboards',
      //   icon: 'dashboards'
      // },
      // {
      //   id: guid(),
      //   name: 'audit-log.audit-logs',
      //   type: 'link',
      //   path: '/auditLogs',
      //   icon: 'track_changes'
      // },
      // {
      //   id: guid(),
      //   name: 'api-usage.api-usage',
      //   type: 'link',
      //   path: '/usage',
      //   icon: 'insert_chart',
      //   notExact: true
      // }
    );
    return sections;
  }

  private buildTenantAdminHome(authUser: any): Array<HomeSection> {
    const homeSections: Array<HomeSection> = [];
    homeSections.push(
      {
        name: 'dft.role.type',
        places: [
          {
            name: 'dft.role.type',
            icon: 'QuanLyVaiTroHome',
            path: '/role'
          }
        ]
      },
      {
        name: 'dft.users.manage',
        places: [
          {
            name: 'dft.users.manage',
            icon: 'QuanLyTaiKhoanHome',
            path: '/account'
          }
        ]
      },
      {
        name: 'dft.damtom.manage',
        places: [
          {
            name: 'dft.damtom.manage',
            icon: 'DamTomHome',
            path: '/dam-tom'
          }
        ]
      },
      {
        name: 'dft.users.access-history',
        places: [
          {
            name: 'dft.users.access-history',
            icon: 'LichSuTruyCapHome',
            path: '/logs'
          }
        ]
      },
      {
        name: 'dft.bao-cao.lap-lich-bc',
        places: [
          {
            name: 'dft.bao-cao.lap-lich-bc',
            path: '/report-schedule',
            icon: 'DatLichXuatBaoCaoHome'
          }
        ]
      },
      {
        name: 'dft.bao-cao.bc-dashboard',
        places: [
          {
            name: 'dft.bao-cao.bc-dashboard',
            path: '/damtom-dashboard',
            icon: 'DashboardHome'
          }
        ]
      },
      {
        name: 'dft.bao-cao.bao-cao',
        places: [
          {
            name: 'dft.bao-cao.bc-tong-hop',
            path: '/bao-cao/bc-tonghop',
            icon: 'BaoCaoTongHopHome'
          },
          {
            name: 'dft.bao-cao.bc-dl-giamsat',
            path: '/bao-cao/bc-dlgiamsat',
            icon: 'BaoCaoDuLieuGiamSatHome'
          },
          {
            name: 'dft.bao-cao.bc-canh-bao',
            path: '/bao-cao/bc-canhbao',
            icon: 'BaoCaoCanhBaoHome'
          },
          {
            name: 'dft.bao-cao.bc-ketnoi-cb',
            path: '/bao-cao/bc-ketnoi-cambien',
            icon: 'BaoCaoCamBienHome'
          },
          {
            name: 'dft.bao-cao.bc-guitt-cb',
            path: '/bao-cao/bc-gui-ttcb',
            icon: 'BaoCaoThongBaoHome'
          }
        ]
      }
      // {
      //   name: 'rulechain.management',
      //   places: [
      //     {
      //       name: 'rulechain.rulechains',
      //       icon: 'settings_ethernet',
      //       path: '/ruleChains'
      //     }
      //   ]
      // },
      // {
      //   name: 'customer.management',
      //   places: [
      //     {
      //       name: 'customer.customers',
      //       icon: 'supervisor_account',
      //       path: '/customers'
      //     }
      //   ]
      // },
      // {
      //   name: 'asset.management',
      //   places: [
      //     {
      //       name: 'asset.assets',
      //       icon: 'domain',
      //       path: '/assets'
      //     }
      //   ]
      // },
      // {
      //   name: 'device.management',
      //   places: [
      //     {
      //       name: 'device.devices',
      //       icon: 'devices_other',
      //       path: '/devices'
      //     },
      //     {
      //       name: 'device-profile.device-profiles',
      //       icon: 'mdi:alpha-d-box',
      //       isMdiIcon: true,
      //       path: '/deviceProfiles'
      //     }
      //   ]
      // },
      // {
      //   name: 'entity-view.management',
      //   places: [
      //     {
      //       name: 'entity-view.entity-views',
      //       icon: 'view_quilt',
      //       path: '/entityViews'
      //     }
      //   ]
      // },
      // {
      //   name: 'dashboard.management',
      //   places: [
      //     {
      //       name: 'widget.widget-library',
      //       icon: 'now_widgets',
      //       path: '/widgets-bundles'
      //     },
      //     {
      //       name: 'dashboard.dashboards',
      //       icon: 'dashboard',
      //       path: '/dashboards'
      //     }
      //   ]
      // },
      // {
      //   name: 'audit-log.audit',
      //   places: [
      //     {
      //       name: 'audit-log.audit-logs',
      //       icon: 'track_changes',
      //       path: '/auditLogs'
      //     },
      //     {
      //       name: 'api-usage.api-usage',
      //       icon: 'insert_chart',
      //       path: '/usage'
      //     }
      //   ]
      // }
    );
    return homeSections;
  }

  private buildCustomerUserMenu(authUser: any): Array<MenuSection> {
    const sections: Array<MenuSection> = [];
    sections.push(
      {
        id: guid(),
        name: 'home.home',
        type: 'link',
        path: '/home',
        icon: 'HouseMenu',
      },
      //   {
      //     id: guid(),
      //     name: 'asset.assets',
      //     type: 'link',
      //     path: '/assets',
      //     icon: 'domain'
      //   },
      //   {
      //     id: guid(),
      //     name: 'device.devices',
      //     type: 'link',
      //     path: '/devices',
      //     icon: 'devices_other'
      //   },
      //   {
      //     id: guid(),
      //     name: 'entity-view.entity-views',
      //     type: 'link',
      //     path: '/entityViews',
      //     icon: 'view_quilt'
      //   },
      //   {
      //     id: guid(),
      //     name: 'dashboard.dashboards',
      //     type: 'link',
      //     path: '/dashboards',
      //     icon: 'dashboard'
      //   }
    );

    // binhdv
    // push them MenuSection dựa vào các permission nhận đc trong authUser.scopes
    for (const x of this.myMenuHelpers) {
      if (this.checkContainAll(authUser.scopes, x.permissions)) {
        sections.push(x.menuSection);
      }
    }

    // build menu bao cao
    const baocaoMenu: MenuSection[] = [];

    for (const x of this.baocaoMenuHelpers) {
      if (this.checkContainAll(authUser.scopes, x.permissions)) {
        baocaoMenu.push(x.menuSection);
      }
    }

    if (baocaoMenu.length > 0) {
      // build menu bao cao :
      sections.push(
        {
          id: guid(),
          name: 'dft.bao-cao.bao-cao',
          type: 'toggle',
          path: '/bao-cao',
          height: '200px',
          icon: 'CalendarBlankMenu',
          pages: baocaoMenu
        }
      );
    }


    return sections;
  }

  private buildCustomerUserHome(authUser: any): Array<HomeSection> {
    const homeSections: Array<HomeSection> = [
      // {
      //   name: 'device.view-devices',
      //   places: [
      //     {
      //       name: 'device.devices',
      //       icon: 'devices_other',
      //       path: '/devices'
      //     }
      //   ]
      // },
      // {
      //   name: 'entity-view.management',
      //   places: [
      //     {
      //       name: 'entity-view.entity-views',
      //       icon: 'view_quilt',
      //       path: '/entityViews'
      //     }
      //   ]
      // },
      // {
      //   name: 'dashboard.view-dashboards',
      //   places: [
      //     {
      //       name: 'dashboard.dashboards',
      //       icon: 'dashboard',
      //       path: '/dashboards'
      //     }
      //   ]
      // }
    ];
    const PAGES_USERS_PERMISSION = [Authority.PAGES_USERS];
    if (this.checkContainAll(authUser.scopes, PAGES_USERS_PERMISSION)) {
      homeSections.push({
        name: 'dft.users.manage',
        places: [
          {
            name: 'dft.users.manage',
            icon: 'QuanLyTaiKhoanHome',
            path: '/account'
          }
        ]
      });
    }

    const PAGES_ROLE_PERMISSION = [Authority.PAGES_ROLES];
    if (this.checkContainAll(authUser.scopes, PAGES_ROLE_PERMISSION)) {
      homeSections.push({
        name: 'dft.role.type',
        places: [
          {
            name: 'dft.role.type',
            icon: 'QuanLyVaiTroHome',
            path: '/role'
          }
        ]
      });
    }

    const PAGES_USERS_HISTORY_PERMISSION = [Authority.PAGES_USERS_ACCESS_HISTORY];
    if (this.checkContainAll(authUser.scopes, PAGES_USERS_HISTORY_PERMISSION)) {
      homeSections.push({
        name: 'dft.users.access-history',
        places: [
          {
            name: 'dft.users.access-history',
            icon: 'LichSuTruyCapHome',
            path: '/logs'
          }
        ]
      });
    }

    const PAGES_DAMTOM_PERMISSION = [Authority.PAGES_DAMTOM];
    if (this.checkContainAll(authUser.scopes, PAGES_DAMTOM_PERMISSION)) {
      homeSections.push({
        name: 'dft.damtom.manage',
        places: [
          {
            name: 'dft.damtom.manage',
            icon: 'MenuService',
            path: '/dam-tom'
          }
        ]
      });
    }

    const PAGES_DATLICH_XUATBC_PERMISSION = [Authority.PAGES_DATLICH_XUATBC];
    if (this.checkContainAll(authUser.scopes, PAGES_DATLICH_XUATBC_PERMISSION)) {
      homeSections.push({
        name: 'dft.bao-cao.lap-lich-bc',
        places: [
          {
            name: 'dft.bao-cao.lap-lich-bc',
            path: '/report-schedule',
            icon: 'DatLichXuatBaoCaoHome'
          }
        ]
      });
    }

    const PAGES_BAOCAODB_PERMISSION = [Authority.PAGES_DAMTOM];
    if (this.checkContainAll(authUser.scopes, PAGES_BAOCAODB_PERMISSION)) {
      homeSections.push({
        name: 'dft.bao-cao.bc-dashboard',
        places: [
          {
            name: 'dft.bao-cao.bc-dashboard',
            path: '/damtom-dashboard',
            icon: 'DashboardHome'
          }
        ]
      });
    }

    const PAGES_BAOCAO_PERMISSION = [Authority.PAGES_BAOCAO];
    if (this.checkContainAll(authUser.scopes, PAGES_BAOCAO_PERMISSION)) {
      const customPlaces = [];
      const PAGES_BAOCAOTH_PERMISSION = [Authority.PAGES_BC_TONGHOP];
      if (this.checkContainAll(authUser.scopes, PAGES_BAOCAOTH_PERMISSION)) {
        customPlaces.push({
          name: 'dft.bao-cao.bc-tong-hop',
          path: '/bao-cao/bc-tonghop',
          icon: 'BaoCaoTongHopHome'
        });
      }
      const PAGES_BAOCAOGS_PERMISSION = [Authority.PAGES_BC_DLGIAMSAT];
      if (this.checkContainAll(authUser.scopes, PAGES_BAOCAOGS_PERMISSION)) {
        customPlaces.push({
          name: 'dft.bao-cao.bc-dl-giamsat',
          path: '/bao-cao/bc-dlgiamsat',
          icon: 'BaoCaoDuLieuGiamSatHome'
        });
      }
      const PAGES_BAOCAOCB_PERMISSION = [Authority.PAGES_BC_CANHBAO];
      if (this.checkContainAll(authUser.scopes, PAGES_BAOCAOCB_PERMISSION)) {
        customPlaces.push({
          name: 'dft.bao-cao.bc-canh-bao',
          path: '/bao-cao/bc-canhbao',
          icon: 'BaoCaoCanhBaoHome'
        });
      }
      const PAGES_BAOCAOKL_PERMISSION = [Authority.PAGES_BC_KETNOI_CAMBIEN];
      if (this.checkContainAll(authUser.scopes, PAGES_BAOCAOKL_PERMISSION)) {
        customPlaces.push(  {
          name: 'dft.bao-cao.bc-ketnoi-cb',
          path: '/bao-cao/bc-ketnoi-cambien',
          icon: 'BaoCaoCamBienHome'
        });
      }
      const PAGES_BAOCAOGTB_PERMISSION = [Authority.PAGES_BC_GUI_TTCB];
      if (this.checkContainAll(authUser.scopes, PAGES_BAOCAOGTB_PERMISSION)) {
        customPlaces.push({
          name: 'dft.bao-cao.bc-guitt-cb',
          path: '/bao-cao/bc-gui-ttcb',
          icon: 'BaoCaoThongBaoHome'
        });
      }
      homeSections.push({
        name: 'dft.damtom.manage',
        places: customPlaces
      });
    }

    return homeSections;
  }

  public menuSections(): Observable<Array<MenuSection>> {
    return this.menuSections$;
  }

  public homeSections(): Observable<Array<HomeSection>> {
    return this.homeSections$;
  }

  private checkContainAll(arr: string[], target: string[]): boolean {
    // check if arr contain all element of target
    return target.every(x => arr.includes(x));
  }

}


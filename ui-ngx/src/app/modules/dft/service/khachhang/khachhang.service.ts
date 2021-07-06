import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {PageLink} from '@shared/models/page/page-link';
import {PageData} from '@shared/models/page/page-data';
import {defaultHttpOptionsFromConfig, RequestConfig} from '@app/core/public-api';
import {KhachHang} from '../../models/khachhang.model';

const QL_KHACHHANG_API = '/api/sys-admin/khach-hang';

@Injectable({
  providedIn: 'root'
})
export class KhachHangService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getListKhachHang(pageLink: PageLink, config?: RequestConfig): Observable<PageData<KhachHang>> {
    return this.http.get<PageData<KhachHang>>(`${QL_KHACHHANG_API}${pageLink.toQuery()}`, defaultHttpOptionsFromConfig(config));
  }

  public getKhachHang(khachhangId: string, config?: RequestConfig): Observable<KhachHang> {
    return this.http.get<KhachHang>(`${QL_KHACHHANG_API}/${khachhangId}`, defaultHttpOptionsFromConfig(config));
  }

  public saveKhachHang(khachhang: KhachHang, config?: RequestConfig): Observable<KhachHang> {
    return this.http.post<KhachHang>(`${QL_KHACHHANG_API}`, khachhang, defaultHttpOptionsFromConfig(config));
  }

  public updateKhachHang(khachhang: KhachHang, khachhangId: string, config?: RequestConfig): Observable<KhachHang> {
    return this.http.put<KhachHang>(`${QL_KHACHHANG_API}/${khachhangId}`, khachhang, defaultHttpOptionsFromConfig(config));
  }

  public deleteKhachHang(khachhangId: string, config?: RequestConfig) {
    return this.http.delete(`${QL_KHACHHANG_API}/${khachhangId}`, defaultHttpOptionsFromConfig(config));
  }

  public uploadTaiLieu(khachHangId: string, formData: FormData): Observable<KhachHang> {
    return this.http.post<KhachHang>(`${QL_KHACHHANG_API}/${khachHangId}/upload-files`, formData, {
      reportProgress: true,
      responseType: 'json'
    });
  }

  public removeTaiLieu(khachHangId: string, listFileRemove: Array<string>): Observable<KhachHang> {
    return this.http.post<KhachHang>(`${QL_KHACHHANG_API}/${khachHangId}/remove-files`,
      listFileRemove, {reportProgress: true, responseType: 'json'});
  }


  public downloadTaiLieu(khachHangId: string, tenTaiLieu: string): Observable<Blob> {
    return this.http.get(`${QL_KHACHHANG_API}/${khachHangId}/download-file?taiLieu=${tenTaiLieu}`, {
      responseType: 'blob'
    });
  }

  public validateKhachHang(query: string, config?: RequestConfig): Observable<boolean> {
    return this.http.get<boolean>(`${QL_KHACHHANG_API}/validate?${query}`, defaultHttpOptionsFromConfig(config));
  }

}

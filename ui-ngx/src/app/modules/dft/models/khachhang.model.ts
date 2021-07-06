export interface KhachHang {
  id: string;
  maKhachHang: string;
  tenKhachHang: string;
  soDienThoai: number;
  email: string;
  ngayBatDau: number;
  active: boolean;
  dsTaiLieu?: Array<string>[];
  ghiChu?: string;
  createdBy?: string;
  createdTime?: number;
}

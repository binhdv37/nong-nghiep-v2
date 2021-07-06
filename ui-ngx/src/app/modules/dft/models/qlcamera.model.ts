export class DamtomCamera{
  id?: string;
  tenantId?: string;
  damtomId: string;
  name: string;
  code: string;
  url: string;
  note: string;
  main: boolean;
  createdBy?: string;
  createdTime?: number;
  errorDto?: EditCamError;
}

export interface EditCamError {
  code: number;
  message: string;

    /*
        - code 1 : mã camera đã tồn tại
        - code 2 : tên camera đã tồn tại
    */
}

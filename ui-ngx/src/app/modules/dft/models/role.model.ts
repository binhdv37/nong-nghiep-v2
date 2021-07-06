export interface Role{
  id?: string;
  tenantId?: string;
  name?: string;
  note?: string;
  permissions?: Permission[];
  createdBy?: string;
  createdTime?: number;
  errorInfo?: ErrorInfo;
}

export interface Permission {
  id?: string;
  tenantId?: string;
  roleId?: string;
  name: string;
  createdBy?: string;
  createdTime?: number;
}

export interface ErrorInfo {
    code?: number;
    message?: string;

    /*
      errorCode : 1 - loi lien quan den validate dto (k dung den bao h)
      errorCode : 2 - ten vai tro da ton tai
    */
}

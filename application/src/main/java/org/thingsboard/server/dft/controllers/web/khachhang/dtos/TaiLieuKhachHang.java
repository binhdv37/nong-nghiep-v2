package org.thingsboard.server.dft.controllers.web.khachhang.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class TaiLieuKhachHang {
    private MultipartFile[] taiLieu;
}

package org.thingsboard.server.dft.controllers.web.khachhang.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class NotificationSetting {
    @NotNull(message = "required")
    private String firebaseApiUrl;

    @NotNull(message = "required")
    private String firebaseAccessToken;


    public String getFirebaseApiUrl() {
        return firebaseApiUrl;
    }

    public String getFirebaseAccessToken() {
        return firebaseAccessToken;
    }
}

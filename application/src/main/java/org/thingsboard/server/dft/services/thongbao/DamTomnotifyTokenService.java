package org.thingsboard.server.dft.services.thongbao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.entities.DamTomNotifyTokenEntity;
import org.thingsboard.server.dft.repositories.DamTomNotifyTokenRepository;

import java.util.UUID;

@Service
@Slf4j
public class DamTomnotifyTokenService extends BaseController {
    @Autowired
    DamTomNotifyTokenRepository damTomNotifyTokenRepository;

    public DamTomNotifyTokenEntity saveToken(DamTomNotifyTokenEntity token) throws ThingsboardException {
        UUID userId = getCurrentUser().getId().getId();
        DamTomNotifyTokenEntity damTomNotifyTokenEntity = damTomNotifyTokenRepository.findFirstByUserId(userId);
        if (damTomNotifyTokenEntity == null) {
//            log.info("tao moi truong du lieu "+ token);

            DamTomNotifyTokenEntity entity = damTomNotifyTokenRepository.findFirstByNotifyToken(token.getNotifyToken());
            if(entity != null){
                damTomNotifyTokenRepository.delete(entity);
            }
            DamTomNotifyTokenEntity damTomNotifyTokenEntity1 = new DamTomNotifyTokenEntity();
            damTomNotifyTokenEntity1.setNotifyToken(token.getNotifyToken());
            damTomNotifyTokenEntity1.setUserId(userId);
            damTomNotifyTokenEntity1.setId(UUID.randomUUID());
            return damTomNotifyTokenRepository.save(damTomNotifyTokenEntity1);
        }else{
//            log.info("update truong du lieu "+ token+ " userid "+userId);
            DamTomNotifyTokenEntity entity = damTomNotifyTokenRepository.findFirstByNotifyToken(token.getNotifyToken());
            if(entity != null){
                damTomNotifyTokenRepository.delete(entity);
            }
            damTomNotifyTokenEntity.setNotifyToken(token.getNotifyToken());
            return damTomNotifyTokenRepository.save(damTomNotifyTokenEntity);
        }
    }

    public DamTomNotifyTokenEntity findByUserId(UUID userId){
        return damTomNotifyTokenRepository.findFirstByUserId(userId);
    }

}

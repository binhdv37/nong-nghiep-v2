package org.thingsboard.server.dft.controllers.web.thongbao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.server.common.data.TenantInfo;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.device.profile.DftAlarmRule;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.model.sql.*;
import org.thingsboard.server.dao.sql.asset.AssetRepository;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dft.common.constants.NotiticationStatusConstant;
import org.thingsboard.server.dft.common.constants.NotiticationTypeConstant;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.NotificationSetting;
import org.thingsboard.server.dft.entities.DamTomEntity;
import org.thingsboard.server.dft.entities.DamTomNotifyTokenEntity;
import org.thingsboard.server.dft.entities.DamTomStaffEntity;
import org.thingsboard.server.dft.repositories.DamTomRepository;
import org.thingsboard.server.dft.repositories.DftAdminSettingsRepository;
import org.thingsboard.server.dft.repositories.DftRelationRepository;
import org.thingsboard.server.dft.services.DamTomAlarm.DamTomAlarmService;
import org.thingsboard.server.dft.services.notificationLog.NotificationLogService;
import org.thingsboard.server.dft.services.rpc.BoDieuKhienService;
import org.thingsboard.server.dft.services.thongbao.DamTomnotifyTokenService;
import org.thingsboard.server.dft.services.usersDft.UsersDftService;
import org.thingsboard.server.service.mail.DefaultMailService;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service
public class ThongBaoService {

    private static final int NOT_VIETTEL = 0;

    private final DftRelationRepository dftRelationRepository;
    private final AssetRepository assetRepository;
    private final DamTomRepository damTomRepository;
    private final DamTomAlarmService damTomAlarmService;
    private final DefaultMailService defaultMailService;
    private final UsersDftService usersDftService;
    private final TenantService tenantService;
    private final BoDieuKhienService boDieuKhienService;
    private final DamTomnotifyTokenService damTomnotifyTokenService;
    private final NotificationLogService notificationLogService;
    private final DftAdminSettingsRepository dftAdminSettingsRepository;
    private static NotificationSetting notificationSetting = null;
    private final ObjectMapper objectMapper ;

    @Autowired
    public ThongBaoService(DftRelationRepository dftRelationRepository,
                           AssetRepository assetRepository,
                           DamTomRepository damTomRepository,
                           DamTomAlarmService damTomAlarmService,
                           DefaultMailService defaultMailService,
                           UsersDftService usersDftService,
                           TenantService tenantService,
                           @Lazy BoDieuKhienService boDieuKhienService,
                           @Lazy DamTomnotifyTokenService damTomnotifyTokenService, NotificationLogService notificationLogService, DftAdminSettingsRepository dftAdminSettingsRepository, ObjectMapper objectMapper) {
        this.dftRelationRepository = dftRelationRepository;
        this.assetRepository = assetRepository;
        this.damTomRepository = damTomRepository;
        this.damTomAlarmService = damTomAlarmService;
        this.defaultMailService = defaultMailService;
        this.usersDftService = usersDftService;
        this.tenantService = tenantService;
        this.boDieuKhienService = boDieuKhienService;
        this.damTomnotifyTokenService = damTomnotifyTokenService;
        this.notificationLogService = notificationLogService;
        this.dftAdminSettingsRepository = dftAdminSettingsRepository;
        this.objectMapper = objectMapper;
    }


    public AssetEntity getAssetFromGatewayId(UUID gatewayId) {
        RelationEntity relationEntity =
                dftRelationRepository.findRelationEntityByToId(gatewayId, "ASSET", "DEVICE");
        AssetEntity assetEntity = assetRepository.findById(relationEntity.getFromId()).get();
        return assetEntity;
    }

    public void sendWarning(Alarm alarm, int number) {
        AssetEntity damTomAsset = getAssetFromGatewayId(alarm.getOriginator().getId());
        DamTomEntity damTomEntity = damTomRepository.findDamTomEntityByAssetId(damTomAsset.getId());
//        DamTomAlarmEntity damTomAlarmEntity = damTomAlarmService.findByDamTomIdAndName(damTomEntity.getId(), alarm.getType());
        List<DeviceProfileAlarm> deviceProfileAlarmList = damTomEntity.getDeviceProfile().toData().getProfileData().getAlarms();
        DftAlarmRule dftAlarmRule = null;
        for(DeviceProfileAlarm dpAlarm : deviceProfileAlarmList){
            if(dpAlarm.getAlarmType().equals(alarm.getType())){
                dftAlarmRule = dpAlarm.getDftAlarmRule();
            }
        }
        List<UUID> listId = new ArrayList();
        List<String> listEmail = new ArrayList();
        List<String> listSms = new ArrayList();
        List<String> listToken = new ArrayList();

        // binhdv - danh sách userId nhận notification
        List<UUID> userIds = new ArrayList<>();

//        if (damTomAlarmEntity.getGroupRpcId() != null) {
//            boDieuKhienService.startGroupRpc(damTomEntity.getTenantId(), damTomAlarmEntity.getGroupRpcId());
//        }

        String EMAIL_SUBJECT = "IoT Đầm tôm - Cảnh báo " + damTomEntity.getName();
        String TITLE_SUBJECT = "Cảnh báo " + damTomEntity.getName();
        String MESSAGE;
        Date timeAlarm = new Date();
        if (number == 1) {
            timeAlarm = new Date(alarm.getEndTs());
        } else if (number == 2) {
            timeAlarm = new Date(alarm.getStartTs());
        }

        MESSAGE =
                "Đầm tôm: " + damTomEntity.getName() + "<br>" +
                        "Cảnh báo: " + alarm.getType() + "<br>" +
                        "Thời gian: " + timeAlarm.toString();
        String SMS_MESSAGE = "IoT Đầm tôm - Cảnh báo " + damTomEntity.getName() + "." +
                "Đầm tôm: " + damTomEntity.getName() + ", " +
                "Cảnh báo: " + alarm.getType() + "," +
                "Thời gian: " + timeAlarm.toString();


        TenantInfo tenant = tenantService.findTenantInfoById(new TenantId(damTomEntity.getTenantId()));
        for (DamTomStaffEntity damTomStaff : damTomEntity.getStaffs()) {
            listId.add(damTomStaff.getStaff().getId());
        }

        for (UUID uuid : listId) {
            UserEntity userEntity = this.usersDftService.findById(uuid);
            UserCredentialsEntity userCredentialsEntity =
                    this.usersDftService.findUserCredentialsByUserId(userEntity.getId());
            if(!userCredentialsEntity.isEnabled()) continue;
            if (!userEntity.getEmail().isEmpty()) {
                listEmail.add(userEntity.getEmail());
            }
            if (!userEntity.getLastName().isEmpty()) {
                listSms.add(userEntity.getLastName());
            }
            DamTomNotifyTokenEntity damTomNotifyTokenEntity = damTomnotifyTokenService.findByUserId(userEntity.getId());
            if (damTomNotifyTokenEntity != null) {
                listToken.add(damTomNotifyTokenEntity.getNotifyToken());

                // binhdv
                userIds.add(damTomNotifyTokenEntity.getUserId());
            }
        }

        UserEntity userEntity = usersDftService.findByTenantIdAndAuthority(tenant.getId().getId(), Authority.TENANT_ADMIN);
        listSms.add(userEntity.getLastName());
        DamTomNotifyTokenEntity damTomNotifyTokenEntity = damTomnotifyTokenService.findByUserId(userEntity.getId());
        if (damTomNotifyTokenEntity != null) {
            listToken.add(damTomNotifyTokenEntity.getNotifyToken());

            // binhdv
            userIds.add(damTomNotifyTokenEntity.getUserId());
        }
        if(dftAlarmRule != null ){
        if (dftAlarmRule.isViaEmail()) {
            System.out.println("via email");
            for (String email : listEmail) {
                try {
                    this.defaultMailService
                            .sendEmail(null, email, EMAIL_SUBJECT, MESSAGE);

                    // binhdv - save log for baocao
                    this.notificationLogService.save(
                            UUID.randomUUID(),
                            tenant.getTenantId().getId(),
                            null,
                            email,
                            null,
                            NotiticationTypeConstant.TYPE_EMAIL,
                            NotiticationStatusConstant.TYPE_SUCCESS,
                            System.currentTimeMillis()
                    );

                } catch (ThingsboardException e) {

                    // binhdv - save log for baocao
                    this.notificationLogService.save(
                            UUID.randomUUID(),
                            tenant.getTenantId().getId(),
                            null,
                            email,
                            null,
                            NotiticationTypeConstant.TYPE_EMAIL,
                            NotiticationStatusConstant.TYPE_ERROR,
                            System.currentTimeMillis()
                    );

                    e.printStackTrace();
                }
            }
            if (!tenant.getEmail().isEmpty()) {
                try {
                    this.defaultMailService
                            .sendEmail(null, tenant.getEmail(), EMAIL_SUBJECT, MESSAGE);

                    // binhdv - save log for baocao
                    this.notificationLogService.save(
                            UUID.randomUUID(),
                            tenant.getTenantId().getId(),
                            null,
                            tenant.getEmail(),
                            null,
                            NotiticationTypeConstant.TYPE_EMAIL,
                            NotiticationStatusConstant.TYPE_SUCCESS,
                            System.currentTimeMillis()
                    );

                } catch (ThingsboardException e) {

                    // binhdv - save log for baocao
                    this.notificationLogService.save(
                            UUID.randomUUID(),
                            tenant.getTenantId().getId(),
                            null,
                            tenant.getEmail(),
                            null,
                            NotiticationTypeConstant.TYPE_EMAIL,
                            NotiticationStatusConstant.TYPE_ERROR,
                            System.currentTimeMillis()
                    );

                    e.printStackTrace();
                }
            }
        }}
        if(dftAlarmRule != null ){
        if (dftAlarmRule.isViaNotification()) {
            try {
                if (listToken.size() != 0) {
                    this.sendPushNotification(listToken, TITLE_SUBJECT, alarm.getType(), damTomEntity);

                    // binhdv - save log for baocao
                    for (UUID userId : userIds) {
                        this.notificationLogService.save(
                                UUID.randomUUID(),
                                tenant.getTenantId().getId(),
                                userId,
                                null,
                                null,
                                NotiticationTypeConstant.TYPE_NOTIFICATION,
                                NotiticationStatusConstant.TYPE_SUCCESS,
                                System.currentTimeMillis()
                        );
                    }
                }

            } catch (JSONException | JsonProcessingException e) {

                // binhdv - save log for baocao
                for (UUID userId : userIds) {
                    this.notificationLogService.save(
                            UUID.randomUUID(),
                            tenant.getTenantId().getId(),
                            userId,
                            null,
                            null,
                            NotiticationTypeConstant.TYPE_NOTIFICATION,
                            NotiticationStatusConstant.TYPE_ERROR,
                            System.currentTimeMillis()
                    );
                }

                e.printStackTrace();
            }

        }}
        if(dftAlarmRule!=null){
            System.out.println("send sms");
            if (dftAlarmRule.isViaSms()) {
                System.out.println("via sms");
                    for(String phoneNumber : listSms){
                        System.out.println(phoneNumber);
                        try {
                            System.out.println((this.sendMessage(phoneNumber,SMS_MESSAGE)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


            }
        }

    }

//    private final String FIREBASE_API_URL = "https://fcm.googleapis.com/fcm/send";
//    private final String FIREBASE_SERVER_KEY = "AAAAm9H_iSI:APA91bHV0PfDhZrvK0EI_KGHVoEHJFvzgx7D8wviih6IMPUQnEX0hUEuIYiiT4EvixXX-wYTu0jxDlW0RT72bn_C178Wx3QH-Q1Xy4nhoYcpzNkd4WAiOPnmNTtXlN-onGKNOc7YjVI5";

    public static void setNotificationSetting(NotificationSetting notify){
        notificationSetting = notify;
    }

    public NotificationSetting getNotificationSetting() throws JsonProcessingException {
        if(notificationSetting != null){
            return notificationSetting;
        }
        AdminSettingsEntity adminSettingsEntity =
                dftAdminSettingsRepository.findAdminSettingsEntityByKey("notification");
        return notificationSetting =
                objectMapper.treeToValue(adminSettingsEntity.getJsonValue(), NotificationSetting.class);
    }

    public void sendPushNotification(List<String> keys, String messageTitle, String message, DamTomEntity damTomEntity) throws JSONException, JsonProcessingException {

        JSONObject msg = new JSONObject();
        msg.put("title", messageTitle);
        msg.put("body", message);
        msg.put("sound", "default_sound");

        JSONObject data = new JSONObject();
        data.put("damTomId", damTomEntity.getId().toString());
        callToFcmServer(msg, keys, data);

    }

    private String callToFcmServer(JSONObject message, List<String> receiverFcmKey, JSONObject data ) throws JSONException, JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap httpHeaders = new LinkedMultiValueMap();
        httpHeaders.set("Authorization", "key=" + this.getNotificationSetting().getFirebaseAccessToken());
        httpHeaders.set("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("notification", message);
        json.put("registration_ids", new JSONArray(receiverFcmKey));
        json.put("data", data);

        HttpEntity<String> httpEntity = new HttpEntity<String>(json.toString(), httpHeaders);
        return restTemplate.postForObject(this.getNotificationSetting().getFirebaseApiUrl(), httpEntity, String.class);
    }

    private void sendSms(String sdt, String noidung) {
        try {
            String recipient = sdt;
            String message = noidung;
            String username = "admin";
            String password = "abc123";
            String originator = "+440987654321";

            String requestUrl = "http://127.0.0.1:9501/api?action=sendmessage&" +
                    "username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8") +
                    "&recipient=" + URLEncoder.encode(recipient, "UTF-8") +
                    "&messagetype=SMS:TEXT" +
                    "&messagedata=" + URLEncoder.encode(message, "UTF-8") +
                    "&originator=" + URLEncoder.encode(originator, "UTF-8") +
                    "&serviceprovider=GSMModem1" +
                    "&responseformat=html";


            URL url = new URL(requestUrl);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();

            System.out.println(uc.getResponseMessage());

            uc.disconnect();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());

        }
    }


    private void sendSms(){

    }

    private static  String API_CHECK_VIETTEL_NUMBER = "http://api-02.worldsms.vn/webapi/sendSMS";
    private static String  AUTHORIZATION_KEY = "Basic bWJmZzI6UEV1Zk42R1Q=";

    /**
     * check so dien thoai co phai viettel
     * @param phoneNumber
     * @return
     */
    protected String sendMessage(String phoneNumber,String message) throws JSONException {
        // convert +84*** to 84***
        if(phoneNumber.startsWith("+84")) phoneNumber = phoneNumber.replace("+","");
        if(phoneNumber.startsWith("0")) phoneNumber = phoneNumber.replaceFirst("0","84");
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap httpHeaders = new LinkedMultiValueMap();
        httpHeaders.set("Authorization",AUTHORIZATION_KEY);
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.set("Accept","application/json");
        JSONObject json = new JSONObject();
        json.put("from", "VienThongMN");
        json.put("to", phoneNumber);
        json.put("text", message);
        json.put("unicode", 1);
        System.out.println("Phone number : "+phoneNumber);
        HttpEntity<String> httpEntity = new HttpEntity<String>(json.toString(), httpHeaders);
        System.out.println(json.toString());
        return restTemplate.postForObject(API_CHECK_VIETTEL_NUMBER, httpEntity, String.class);
    }
}

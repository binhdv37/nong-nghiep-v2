package org.thingsboard.server.dft.controllers.web.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.controllers.web.users.dtos.ActivateAccountDto;
import org.thingsboard.server.dft.controllers.web.users.dtos.CheckActiveDto;
import org.thingsboard.server.dft.entities.UserActiveEntity;
import org.thingsboard.server.dft.services.usersActive.UserActiveService;
import org.thingsboard.server.dft.services.usersDft.UsersDftService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.mail.DefaultMailService;

import javax.validation.Valid;

@RestController
@TbCoreComponent
@RequestMapping("/public-api")
public class UserActiveController extends BaseController {

    public static final String EMAIL_SUBJECT = "IOT Nông Nghiệp - Kích hoạt Tài khoản";

    private final UsersDftService usersDftService;
    private final UserActiveService userActiveService;
    private final DefaultMailService defaultMailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserActiveController(
            UsersDftService usersDftService,
            UserActiveService userActiveService,
            DefaultMailService defaultMailService
    ) {
        this.usersDftService = usersDftService;
        this.userActiveService = userActiveService;
        this.defaultMailService = defaultMailService;
    }

    // api cu

    // mobile
    // check xem user da kich hoat tk hay chua
    /*
        - response :
            + 404 : Email not found
            + Http.ok(true) : da kich hoat
            + Http.ok(false) : chua kich hoat
     */
    @GetMapping("/users/check-active")
    public ResponseEntity<?> checkUserIsActive(
            @RequestParam(name = "email") String email)
            throws ThingsboardException {
        try {
            // find user :
            UserEntity userEntity = this.usersDftService.findAllByEmail(email);
            if (userEntity == null) {
                return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(userActiveService
                    .checkUserIsActive(userEntity.getTenantId(), userEntity.getId()));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // gen ma kich hoat, gui qua mail nguoi dung
    /*
        - response :
            + 404 : Email not found
            + 400 bad request : account already been activate || account  sysadmin, cannot activate
            + Http.ok("ma kich hoat"), send code to user's email
     */
    @GetMapping("/users/activate-code")
    public ResponseEntity<String> getActivateCode(
            @RequestParam(name = "email", required = true) String email)
            throws ThingsboardException {
        try {
            // find user :
            UserEntity userEntity = this.usersDftService.findAllByEmail(email);
            if (userEntity == null) {
                return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
            }

            // find record
            UserActiveEntity userActiveEntity = userActiveService
                    .findByTenantIdAndUserId(userEntity.getTenantId(), userEntity.getId());

            // th có tài khoản mà k có record trong damtom_user_active -> là sysadmin
            if (userActiveEntity == null) {
                return new ResponseEntity<>("2",
                        HttpStatus.BAD_REQUEST);
            }

            // check if user is already activate
            if (userActiveEntity.isActive()) {
                return new ResponseEntity<>("1",
                        HttpStatus.BAD_REQUEST);
            }

            // gen ma kich hoat moi, update vao db
            userActiveEntity.setActiveCode(userActiveService.genRandomActivateCode());

            // save to db
            UserActiveEntity activeEntity = userActiveService.save(userActiveEntity);

            // send activate code to email to user
            this.defaultMailService
                    .sendEmail(null, email, EMAIL_SUBJECT, activeEntity.getActiveCode());

            return ResponseEntity.ok(activeEntity.getActiveCode());
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    // activate account :
    /*
        - 4 th response :
            + 404 : Email not found
            + 400 bad request : (1) Account already been activate || (2) account is sysadmin, cannot activate
            + Http.ok(1) : thanh cong
            + Http.ok(0) : sai ma kich hoat
     */
    @PostMapping("/users/activate-account")
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivateAccountDto activateAccountDto)
            throws ThingsboardException {
        try {
            // find user :
            UserEntity userEntity = this.usersDftService
                    .findAllByEmail(activateAccountDto.getEmail());
            if (userEntity == null) {
                return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
            }

            // find record
            UserActiveEntity userActiveEntity = userActiveService
                    .findByTenantIdAndUserId(userEntity.getTenantId(), userEntity.getId());

            //  userActiveEntity == null => tk la sysadmin
            if (userActiveEntity == null) {
                return new ResponseEntity<>("2", HttpStatus.BAD_REQUEST);
            }

            // check if user is already activate
            if (userActiveEntity.isActive()) {
                return new ResponseEntity<>("1", HttpStatus.BAD_REQUEST);
            }

            // check activate code
            if (activateAccountDto.getActivateCode().equals(userActiveEntity.getActiveCode())) {
                // update db, return
                userActiveEntity.setActive(true);
                userActiveService.save(userActiveEntity);
                return ResponseEntity.ok(1);
            }
            return ResponseEntity.ok(0);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    // / api cu




    // mobile
    // check xem user da kich hoat tk hay chua
    /*
        Luồng :
            - check xem username và password có đúng k
            - check tk có bị khóa k
            - check xem active hay chưa
     */
    /*
        - response :
            + 400 : email or password blank
            + 400 : 0 - password incorrect
            + 400 : 1 - tk bi khoa
            + 404 : Email not found
            + Http.ok(true) : da kich hoat
            + Http.ok(false) : chua kich hoat
            + cac th khac(500,...) : server loi
     */
    @PostMapping("/users/check-active-v2")
    public ResponseEntity<?> checkUserIsActive(@Valid @RequestBody CheckActiveDto checkActiveDto)
            throws ThingsboardException {
        try {
            // find user :
            UserEntity userEntity = this.usersDftService.findAllByEmail(checkActiveDto.getEmail());

            if (userEntity == null) {
                return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
            }

            // kiem tra xem mk dung k.
            UserCredentialsEntity userCredentialsEntity = usersDftService.findUserCredentialsByUserId(userEntity.getId());

            if (!passwordEncoder.matches(checkActiveDto.getPassword(), userCredentialsEntity.getPassword())) {
                // password incorrect
                return new ResponseEntity<>("0", HttpStatus.BAD_REQUEST);
            }

            //log
            System.out.println(" ----- 1 : " + userCredentialsEntity.isEnabled() + " ; " + checkActiveDto.getEmail());

            // check xem tk có bị khóa k
            if(!userCredentialsEntity.isEnabled()){
                System.out.println(" ----- 2 : " + userCredentialsEntity.isEnabled() + " ; " + checkActiveDto.getEmail());
                return new ResponseEntity<>("1", HttpStatus.BAD_REQUEST);
            }

            System.out.println(" ----- 3 : " + userCredentialsEntity.isEnabled() + " ; " + checkActiveDto.getEmail());


            return ResponseEntity.ok(userActiveService
                    .checkUserIsActive(userEntity.getTenantId(), userEntity.getId()));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // gen ma kich hoat, gui qua mail nguoi dung
    /*
        - response :
            + 400 : email or password blank
            + 404 : Email not found
            + 400 bad request :
                - (0) - sai mk
                - (1) - tk bị khóa
                - (2) - account is sysadmin, cannot activate
                - (3) - account already been activate
            + Http.ok("ma kich hoat"), send code to user's email
            + cac th khac (500, ...) : loi server
     */
    @PostMapping("/users/activate-code-v2")
    public ResponseEntity<String> sendActivateCode(@Valid @RequestBody CheckActiveDto checkActiveDto)
            throws ThingsboardException {
        try {
            // find user :
            UserEntity userEntity = this.usersDftService.findAllByEmail(checkActiveDto.getEmail());

            if (userEntity == null) {
                return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
            }

            // kiem tra xem mk dung k.
            UserCredentialsEntity userCredentialsEntity = usersDftService.findUserCredentialsByUserId(userEntity.getId());

            if (!passwordEncoder.matches(checkActiveDto.getPassword(), userCredentialsEntity.getPassword())) {
                // mk k dung
                return new ResponseEntity<>("0", HttpStatus.BAD_REQUEST);
            }

            // check xem tk có bị khóa k
            if(!userCredentialsEntity.isEnabled()){
                return new ResponseEntity<>("1", HttpStatus.BAD_REQUEST);
            }

            // find record
            UserActiveEntity userActiveEntity = userActiveService
                    .findByTenantIdAndUserId(userEntity.getTenantId(), userEntity.getId());

            // th có tài khoản mà k có record trong damtom_user_active -> là sysadmin
            if (userActiveEntity == null) {
                return new ResponseEntity<>("2", HttpStatus.BAD_REQUEST);
            }

            // check if user is already activate
            if (userActiveEntity.isActive()) {
                return new ResponseEntity<>("3", HttpStatus.BAD_REQUEST);
            }

            // gen ma kich hoat moi, update vao db
            userActiveEntity.setActiveCode(userActiveService.genRandomActivateCode());

            // save to db
            UserActiveEntity activeEntity = userActiveService.save(userActiveEntity);

            // send activate code to email to user
            this.defaultMailService
                    .sendEmail(null, checkActiveDto.getEmail(), EMAIL_SUBJECT, createMailMessage(userEntity.getFirstName(), activeEntity.getActiveCode()));

            return ResponseEntity.ok(activeEntity.getActiveCode());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    // activate account :
    /*
        - 4 th response :
            + 400 : email or activate code blank
            + 404 : Email not found
            + 400 bad request :
                - (2) account is sysadmin, cannot activate
                - (3) Account already been activate
            + Http.ok(1) : thanh cong
            + Http.ok(0) : sai ma kich hoat
            + cac th khac (500, ...) : loi server
     */
    @PostMapping("/users/activate-account-v2")
    public ResponseEntity<?> activateAccountV2(@Valid @RequestBody ActivateAccountDto activateAccountDto)
            throws ThingsboardException {
        try {
            // find user :
            UserEntity userEntity = this.usersDftService
                    .findAllByEmail(activateAccountDto.getEmail());
            if (userEntity == null) {
                return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
            }

            // find record
            UserActiveEntity userActiveEntity = userActiveService
                    .findByTenantIdAndUserId(userEntity.getTenantId(), userEntity.getId());

            //  userActiveEntity == null => tk la sysadmin
            if (userActiveEntity == null) {
                return new ResponseEntity<>("2", HttpStatus.BAD_REQUEST);
            }

            // check if user is already activate
            if (userActiveEntity.isActive()) {
                return new ResponseEntity<>("3", HttpStatus.BAD_REQUEST);
            }

            // check activate code
            if (activateAccountDto.getActivateCode().equals(userActiveEntity.getActiveCode())) {
                // update db, return
                userActiveEntity.setActive(true);
                userActiveService.save(userActiveEntity);
                return ResponseEntity.ok(1);
            }
            return ResponseEntity.ok(0);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private String createMailMessage(String fullName, String activateCode) {
        return "Xin chào " + fullName + "! <br>" +
                "<br>" +
                "Mã kích hoạt Tài khoản của bạn là: <br>" +
                activateCode + " <br>" +
                "<br>" +
                "Vui lòng nhập Mã kích hoạt này để Kích hoạt Tài khoản và bắt đầu sử dụng." + "<br>" +
                "<br>" +
                "Xin cảm ơn!";
    }

}

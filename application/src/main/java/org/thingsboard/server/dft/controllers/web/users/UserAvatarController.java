package org.thingsboard.server.dft.controllers.web.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.sql.user.UserRepository;
import org.thingsboard.server.dft.controllers.web.users.dtos.FileName;
import org.thingsboard.server.dft.entities.UserAvatarEntity;
import org.thingsboard.server.dft.repositories.UserAvatarRepository;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class UserAvatarController extends BaseController {

  private final UserAvatarRepository userAvatarRepository;
  private final UserRepository userRepository;

  @Autowired
  public UserAvatarController(
      UserAvatarRepository userAvatarRepository, UserRepository userRepository) {
    this.userAvatarRepository = userAvatarRepository;
    this.userRepository = userRepository;
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/users/avatar")
  @ResponseBody
  public ResponseEntity<?> saveAvatar(@RequestBody FileName fileName) {
    try {
      UUID userId = getCurrentUser().getId().getId();
      UserAvatarEntity userAvatarEntity = userAvatarRepository.findUserAvatarEntityByUserId(userId);
      if (userAvatarEntity == null) {
        userAvatarEntity = new UserAvatarEntity();
        userAvatarEntity.setId(UUID.randomUUID());
      }
      userAvatarEntity.setUserId(userId);
      userAvatarEntity.setAvatar(fileName.getFileName());
      return new ResponseEntity<>(userAvatarRepository.save(userAvatarEntity), HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/users/{id}/avatar")
  public ResponseEntity<?> getUserAvatar(@PathVariable("id") UUID userId) {
    try {
      UserAvatarEntity avatarEntity = userAvatarRepository.findUserAvatarEntityByUserId(userId);
      if (avatarEntity == null) {
        return new ResponseEntity<>(HttpStatus.OK);
      } else {
        return new ResponseEntity<>(avatarEntity.getAvatar(), HttpStatus.OK);
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }
}

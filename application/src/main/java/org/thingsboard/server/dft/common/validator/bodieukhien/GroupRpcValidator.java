package org.thingsboard.server.dft.common.validator.bodieukhien;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.repositories.GroupRpcRepository;

import java.util.UUID;

@Component
public class GroupRpcValidator {
  public static final String TYPE_ADD = "ADD_ENTITY";
  public static final String TYPE_EDIT = "EDIT_ENTITY";

  private final GroupRpcRepository groupRpcRepository;

  @Autowired
  public GroupRpcValidator(GroupRpcRepository groupRpcRepository) {
    this.groupRpcRepository = groupRpcRepository;
  }

  public boolean validateGroupRpc(UUID damTomId, String type, String value, UUID id) {
    String groupRpcName = value.trim();
    switch (type) {
      case TYPE_ADD:
        return groupRpcRepository.existsGroupRpcEntitiesByDamTomIdAndTen(damTomId, groupRpcName);
      case TYPE_EDIT:
        return groupRpcRepository.existsGroupRpcEntitiesByDamTomIdAndTenAndIdNot(
            damTomId, groupRpcName, id);
      default:
        return false;
    }
  }
}

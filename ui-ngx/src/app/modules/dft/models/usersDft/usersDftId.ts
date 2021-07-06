import {EntityId} from "@shared/models/id/entity-id";
import {EntityType} from "@shared/models/entity-type.models";

export class UsersDftId implements EntityId {
  entityType = EntityType.USER_DFT;
  id: string;
  constructor(id: string) {
    this.id = id;
  }
}

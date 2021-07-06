import { EntityId, EntityType } from '@app/shared/public-api';

export class DamTomId implements EntityId {
  entityType = EntityType.DAMTOM;
  id: string;
  constructor(id: string) {
    this.id = id;
  }
}

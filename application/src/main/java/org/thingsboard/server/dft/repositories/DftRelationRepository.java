package org.thingsboard.server.dft.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.RelationCompositeKey;
import org.thingsboard.server.dao.model.sql.RelationEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DftRelationRepository
    extends CrudRepository<RelationEntity, RelationCompositeKey>,
        JpaSpecificationExecutor<RelationEntity> {

  List<RelationEntity> findAllByFromIdAndFromType(UUID fromId, String fromType);

  @Query(
      value =
          "select r.* from relation r join device d on r.to_id = d.id "
              + "where r.from_id = :fromId and r.from_type = :fromType and r.to_type = :toType "
              + "order by d.created_time asc",
      nativeQuery = true)
  List<RelationEntity> findAllByFromIdAndFromTypeAndToType(
      @Param("fromId") UUID fromId,
      @Param("fromType") String fromType,
      @Param("toType") String toType);

  @Query(
      value =
          "select r.* from relation r  where r.to_id = :toId and "
              + "r.from_type = :fromType and r.to_type = :toType ",
      nativeQuery = true)
  RelationEntity findRelationEntityByToId(
      @Param("toId") UUID toId, @Param("fromType") String fromType, @Param("toType") String toType);
}

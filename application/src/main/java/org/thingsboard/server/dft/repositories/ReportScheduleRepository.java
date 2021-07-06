package org.thingsboard.server.dft.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.entities.ReportScheduleEntity;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportScheduleEntity, UUID> {

    List<ReportScheduleEntity> getAllByActive(boolean active);


    /**
     * Query All
     * @author viettd
     */
    @Query("SELECT s FROM ReportScheduleEntity s WHERE s.tenantId = :tenantId " +
            "AND LOWER(s.reportName) LIKE  LOWER(CONCAT('%', :searchText, '%'))")
    Page<ReportScheduleEntity> findByTenantId(@Param("tenantId") UUID tenantId, @Param("searchText") String searchText, Pageable pageable);

    /**
     * @mobile
     * check schedule name exist create
     * @author viettd
     */
    boolean existsByTenantIdAndScheduleName(UUID tenantId,String scheduleName);

    /**
     * @mobile
     * check schedule name exist create
     * @author viettd
     */
    boolean existsByTenantIdAndIdNotAndScheduleName(UUID tenantId,UUID scheduleId,String scheduleName);
}

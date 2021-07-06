package org.thingsboard.server.dft.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.model.sql.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends PagingAndSortingRepository<UserEntity, UUID> {

    @Query("SELECT u FROM UserEntity u JOIN UserCredentialsEntity uc ON u.id = uc.userId WHERE u.tenantId = :tenantId " +
            "AND (u.authority = 'CUSTOMER_USER')" +
            "AND (LOWER(u.searchText) LIKE LOWER(CONCAT('%', :searchText, '%'))" + //email
            "OR LOWER(u.firstName) LIKE  LOWER(CONCAT('%', :searchText, '%'))" +
            "OR LOWER(u.lastName) LIKE  LOWER(CONCAT('%', :searchText, '%')))") //tim theo sdt - for a tuan
    Page<UserEntity> findByTenantId(@Param("tenantId") UUID tenantId, @Param("searchText") String searchText, Pageable pageable);

    // mobile
    @Query("SELECT u FROM UserEntity u WHERE u.tenantId = :tenantId AND (u.authority = 'CUSTOMER_USER')")
    List<UserEntity> findByTenantId(@Param("tenantId") UUID tenantId);

    UserEntity findAllByEmail(String email);

    UserEntity findAllByLastName(String lastName);

    @Query("select u.email from UserEntity u where lower(u.email) like lower(:email) and u.id <> :id ")
    UserEntity findAllByEmailAndIdNotEqual(@Param("email") String email, @Param("id") UUID id);

    @Query("select u.lastName from UserEntity u where lower(u.lastName) like lower(:lastName) and u.id <> :id ")
    UserEntity findAllByLastNameAndIdNotEqual(@Param("lastName") String lastName, @Param("id") UUID id);

    Optional<UserEntity> findById(UUID id);

    // binhdv - check trung sdt
    boolean existsByLastName(String phoneNumber);

    // binhdv - check update sdt da ton tai:
    boolean existsByIdIsNotAndLastNameEquals(UUID userId, String phoneNumber);

    List<UserEntity> findByTenantIdAndAuthority(UUID tenantId, Authority values);
}

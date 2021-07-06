package org.thingsboard.server.dft.entities;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "damtom_group_rpc")
public class GroupRpcEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "damtom_id")
    private UUID damTomId;

    @Column(name = "ten")
    private String ten;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_time")
    private long createdTime;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "groupRpcId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<RpcSettingEntity> rpcSettingEntities;


}

package br.com.jonas.multitenant.user.entity;

import br.com.jonas.multitenant.tenant.entity.Tenant;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_tenant_username",
                        columnNames = {"tenant_id", "username"}
                )
        }
)
public class User {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    protected User() {
    }

    public User(
            UUID id,
            Tenant tenant,
            String username,
            String password
    ) {
        this.id = id;
        this.tenant = tenant;
        this.username = username;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
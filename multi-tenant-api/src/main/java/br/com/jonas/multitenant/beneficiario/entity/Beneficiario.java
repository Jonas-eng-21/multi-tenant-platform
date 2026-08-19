package br.com.jonas.multitenant.beneficiario.entity;

import br.com.jonas.multitenant.pessoa.entity.Pessoa;
import br.com.jonas.multitenant.tenant.entity.Tenant;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "beneficiarios",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_beneficiarios_tenant_matricula",
                        columnNames = {"tenant_id", "matricula"}
                )
        }
)
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 50)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBeneficiario tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusBeneficiario status;

    @Column(name = "data_adesao", nullable = false)
    private LocalDate dataAdesao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Beneficiario() {
    }

    public Beneficiario(
            UUID id,
            Pessoa pessoa,
            Tenant tenant,
            String matricula,
            TipoBeneficiario tipo,
            StatusBeneficiario status,
            LocalDate dataAdesao
    ) {
        this.id = id;
        this.pessoa = pessoa;
        this.tenant = tenant;
        this.matricula = matricula;
        this.tipo = tipo;
        this.status = status;
        this.dataAdesao = dataAdesao;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getMatricula() {
        return matricula;
    }

    public TipoBeneficiario getTipo() {
        return tipo;
    }

    public StatusBeneficiario getStatus() {
        return status;
    }

    public LocalDate getDataAdesao() {
        return dataAdesao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void setTipo(TipoBeneficiario tipo) {
        this.tipo = tipo;
    }

    public void setStatus(StatusBeneficiario status) {
        this.status = status;
    }

    public void setDataAdesao(LocalDate dataAdesao) {
        this.dataAdesao = dataAdesao;
    }
}

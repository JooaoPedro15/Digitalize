package com.digitalize.canal;

import com.digitalize.empresa.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** Perfil de uma empresa em uma rede social (ex.: Instagram @docelar). */
@Entity
@Table(name = "canal")
public class Canal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 20)
    private String plataforma;

    @Column(nullable = false, length = 80)
    private String identificador;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Canal() {
    }

    public Canal(Empresa empresa, String plataforma, String identificador) {
        this.empresa = empresa;
        this.plataforma = plataforma;
        this.identificador = identificador;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getIdentificador() {
        return identificador;
    }
}

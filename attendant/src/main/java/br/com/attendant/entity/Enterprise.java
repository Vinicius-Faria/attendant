package br.com.attendant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "enterprise")
@Getter @Setter
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "enterprise_type")
    private EnterpriseType enterpriseType;

    private String cnpj;

}

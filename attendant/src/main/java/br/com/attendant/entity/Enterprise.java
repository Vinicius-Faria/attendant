package br.com.attendant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "enterprise")
@Getter @Setter
@NoArgsConstructor
public class Enterprise {

    public Enterprise(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "enterprise_type")
    private EnterpriseType enterpriseType;

    private String cnpj;

}

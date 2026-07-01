package br.com.attendant.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "enterprise")
@Getter @Setter
@NoArgsConstructor
@Schema(description = "Representação da entidade de Enterprise")
public class Enterprise {

    public Enterprise(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID exclusivo da empresa", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome Fantasia da Empresa")
    private String descricao;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "enterprise_type")
    @Schema(description = "Tipo da empresa", example = "BARBER")
    private EnterpriseType enterpriseType;

    @Schema(description = "CNPJ da Empresa")
    private String cnpj;

}

package br.com.attendant.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Entity
@Table(name = "service_enterprise")
@Getter
@Setter
@Schema(description = "Representação da entidade de ServiceEnterprise")
public class ServiceEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID exclusivo da empresa", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Descrição do serviço da Empresa")
    private String descricao;

    @Schema(description = "Valor do serviço", example = "10.00")
    private Double price;

    @Schema(description = "Duração do serviço. (**SEMPRE EM MINUTOS**)", example = "30")
    private Integer duration;

    @JoinColumn(name = "enterprise_id")
    @ManyToOne(fetch = FetchType.EAGER)
    @Schema(description = "Empresa vinculado ao Serviço")
    private Enterprise enterprise;

    @Column(name = "has_time_between_one_service_and_another")
    @Schema(description = "Existe tempo entre um serviço e outro", example = "true")
    private Boolean hasTimeBetweenOneServiceAndAnother;

    @Column(name = "time_between_one_service_and_another")
    @Schema(description = "Quando tempo entre um serviço e outro. (**SEMPRE EM MINUTOS**)", example = "5")
    private Integer timeBetweenOneServiceAndAnother;

}

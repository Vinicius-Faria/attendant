package br.com.attendant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Entity
@Table(name = "service_enterprise")
@Getter
@Setter
public class ServiceEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private Double price;

    private Integer duration;

    @JoinColumn(name = "enterprise_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Enterprise enterprise;

    @Column(name = "has_time_between_one_service_and_another")
    private Boolean hasTimeBetweenOneServiceAndAnother;

    @Column(name = "time_between_one_service_and_another")
    private Integer timeBetweenOneServiceAndAnother;

}

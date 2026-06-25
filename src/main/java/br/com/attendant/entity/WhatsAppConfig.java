package br.com.attendant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "whatsapp_config")
@Getter
@Setter
public class WhatsAppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "enterprise_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Enterprise enterprise;

    @Column(name = "number_phone")
    private String numberPhone;

    @Column(name = "wa_business_id")
    private String waBusinessId;

    @Column(name = "api_token")
    private String apiToken;

}
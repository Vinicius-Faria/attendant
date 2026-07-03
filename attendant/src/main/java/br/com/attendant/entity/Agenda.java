package br.com.attendant.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "agenda")
@Getter
@Setter
@Schema(description = "Representação da entidade de Agendamento")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID exclusivo do agendamento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_enterprise_id")
    @Schema(description = "Serviço vinculado a esta agenda")
    private ServiceEnterprise serviceEnterprise;

    @ManyToOne
    @JoinColumn(name = "time_tables_enterprise_id")
    @Schema(description = "Grade horária da empresa vinculada")
    private TimeTablesEnterprise timeTablesEnterprise;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    @Schema(description = "Empresa dona da agenda")
    private Enterprise enterprise;

    @ManyToOne
    @JoinColumn(name = "chat_session_id")
    @Schema(description = "Mensagem de chat que originou o agendamento")
    private ChatSession chatSession;

    @Column(name = "is_active")
    @Schema(description = "Status de ativação do agendamento", example = "true")
    private Boolean isActive;

    @Column(name = "scheduled_for")
    @Schema(description = "Identificação de quem agendou ou origem do agendamento", example = "Fulano")
    private String scheduledFrom;

    @Column(name = "at_date_hour")
    @Schema(description = "Data e hora do agendamento", example = "2026-07-01T14:30:00")
    private LocalDateTime atDateHour;

}
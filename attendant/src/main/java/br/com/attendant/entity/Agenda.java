package br.com.attendant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "agenda")
@Getter
@Setter
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_enterprise_id")
    private ServiceEnterprise serviceEnterprise;

    @ManyToOne
    @JoinColumn(name = "time_tables_enterprise_id")
    private TimeTablesEnterprise timeTablesEnterprise;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @ManyToOne
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @Column(name = "is_active")
    private Boolean IsActive;

    @Column(name = "scheduled_for")
    private String scheduledFrom;

    @Column(name = "at_date_hour")
    private LocalDateTime atDateHour;

}

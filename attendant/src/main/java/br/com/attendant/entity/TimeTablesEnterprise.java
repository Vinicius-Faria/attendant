package br.com.attendant.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "time_tables_enterprise")
@Getter
@Setter
@Schema(description =
        "Representação da entidade de TimeTablesEnterprise. " +
                "Normalmente sempre será 7 dias novos cadastrados para cada empresa." +
                "Representando de Segunda-feira até Domingo.")
public class TimeTablesEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID exclusivo do dia da semana e horário de funcionanmento da empresa",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Dia da Semana", example = "0 - Segunda-feira, 1 - Terça-feira,...")
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Horário que abre", example = "08:00:00")
    @Column(name = "start_time")
    private LocalTime startTime;

    @Schema(description = "Horário que fecha", example = "17:00:00")
    @Column(name = "end_time")
    private LocalTime endTime;

    @Schema(description = "Esse dia estará aberto", example = "true")
    @Column(name = "is_closed")
    private Boolean isClosed;

    @JoinColumn(name = "enterprise_id")
    @ManyToOne(fetch = FetchType.EAGER)
    @Schema(description = "Empresa que irá ter esse horário")
    private Enterprise  enterprise;


}

package br.com.attendant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "time_tables_enterprise")
@Getter
@Setter
public class TimeTablesEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_closed")
    private Boolean isClosed;

    @JoinColumn(name = "enterprise_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Enterprise  enterprise;


}

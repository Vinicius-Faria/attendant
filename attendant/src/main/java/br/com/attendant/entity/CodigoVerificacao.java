package br.com.attendant.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigo_verificacao")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Representação da entidade de CodigoVerificacao")
public class CodigoVerificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID exclusivo do Codigo Verificacao", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    @Schema(description = "Email da empresa")
    private String email;

    @Column(length = 4)
    @Schema(description = "Código gerado pelo projeto de 6 digitos")
    private String codigo;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    @Schema(description = "Data de criaçao")
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    @Schema(description = "Data/Hora de atualizaçao")
    private LocalDateTime atualizadoEm;
}

package br.com.attendant.entity;

import lombok.Getter;

@Getter
public enum EnterpriseType {

    BARBER(1, "Barbearia");

    EnterpriseType(Integer value, String descricao) {
        this.value = value;
        this.descricao = descricao;
    }

    private Integer value;

    private String descricao;

}

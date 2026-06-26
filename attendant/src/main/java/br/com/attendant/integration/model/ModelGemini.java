package br.com.attendant.integration.model;

import lombok.Getter;

@Getter
public enum ModelGemini {

    /**
     * O modelo GEMINI_2_5_FLASH_LITE, pode ser usado para testes locais, mas nao para prod. É muito ruim
     */

    GEMINI_2_5_FLASH(1, "gemini-2.5-flash", true),
    GEMINI_2_5_FLASH_LITE(2, "gemini-2.5-flash-lite", true);

    ModelGemini(Integer id, String model, Boolean free){
        this.id = id;
        this.model = model;
        this.free = free;
    };

    private final Integer id;

    private final String model;

    private final Boolean free;

}

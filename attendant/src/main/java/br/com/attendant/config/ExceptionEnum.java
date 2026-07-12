package br.com.attendant.config;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionEnum {

    GENERIC(0, "Desculpe, estamos com problema para responder e retornaremos assim que possivel", "Generic", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND(1, "Solicitação não encontrada.", "Not Found", HttpStatus.NOT_FOUND),
    ENTITY_INCOMPLETE(2, "Preencha todos os campos.", "Bad Request", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String descricao;
    private final String error;
    private final HttpStatus httpStatus;

    ExceptionEnum(int code, String descricao, String error, HttpStatus httpStatus) {
        this.code = code;
        this.descricao = descricao;
        this.error = error;
        this.httpStatus = httpStatus;
    }


}

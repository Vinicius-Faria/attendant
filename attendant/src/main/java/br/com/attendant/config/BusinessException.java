package br.com.attendant.config;

public class BusinessException extends RuntimeException {

    private final ExceptionEnum exceptionEnum;
    private final String descricao;

    public BusinessException(ExceptionEnum exceptionEnum) {
        this(exceptionEnum, null);
    }

    public BusinessException(ExceptionEnum exceptionEnum, String descricao) {
        super(descricao != null ? descricao : exceptionEnum.getDescricao());
        this.exceptionEnum = exceptionEnum;
        this.descricao = descricao;
    }

    public ExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }

    public String getDescricao() {
        return descricao != null ? descricao : exceptionEnum.getDescricao();
    }
}

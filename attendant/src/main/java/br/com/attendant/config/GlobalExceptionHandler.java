package br.com.attendant.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
        HttpStatus status = ex.getExceptionEnum().getHttpStatus();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getDescricao());
        problemDetail.setTitle(ex.getExceptionEnum().getError());
        problemDetail.setProperty("codigoErro", ex.getExceptionEnum() != null ? ex.getExceptionEnum().name() : "BUSINESS_ERROR");
        return ResponseEntity.status(status).body(problemDetail);
    }

    /**
     * @deprecated
     * Não use essa classe. Caso tenha algum log fazendo apontamento para essa classe. Verifique e tente usar a função: GlobalExceptionHandle.handleBusinessException(BusinessException ex);
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor."
        );
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setProperty("codigoErro", "INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}
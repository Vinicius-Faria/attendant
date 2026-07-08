package br.com.attendant.service;

import br.com.attendant.entity.Enterprise;

public interface CodigoVerificacaoService {
    void enviarCodigo(Enterprise enterprise);
    void reenviarCodigo(String email);
    boolean verificaCodigoByUser(String codigo, Enterprise enterprise);
}

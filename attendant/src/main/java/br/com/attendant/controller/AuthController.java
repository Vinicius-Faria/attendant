package br.com.attendant.controller;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.config.JwtUtil;
import br.com.attendant.dto.CredencialDto;
import br.com.attendant.entity.EnterpriseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;

    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/auth")
    public ResponseEntity<?> login(@RequestBody CredencialDto credencialDto) {

        try {
            UsernamePasswordAuthenticationToken loginData =
                    new UsernamePasswordAuthenticationToken(
                            credencialDto.getEmail(),
                            credencialDto.getSenha()
                    );

            Authentication authentication = authenticationManager.authenticate(loginData);

            EnterpriseDetails usuarioLogado = (EnterpriseDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(usuarioLogado);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", token, "uuid", usuarioLogado.getUuid()));
        } catch (DisabledException e) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND, "Credencial Inválida");

        } catch (BadCredentialsException e) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND, "Credencial Inválida");
        }
    }

}
package br.com.attendant.controller;

import br.com.attendant.entity.ChatMessage;
import br.com.attendant.service.WhatsAppMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/whatsapp-received-simulation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "WhatsApp Simulation", description = "Ambiente de testes locais para simular o tráfego do WhatsApp sem custos com API externa")
public class WhatsAppReceivedSimulationController {

    private final WhatsAppMessageService whatsAppMessageService;

    public WhatsAppReceivedSimulationController(WhatsAppMessageService whatsAppMessageService) {
        this.whatsAppMessageService = whatsAppMessageService;
    }

    @GetMapping("/message-received")
    @Operation(
            summary = "Simular recebimento de mensagem",
            description = "Injeta uma mensagem de texto no fluxo do sistema para simular o comportamento do webhook do WhatsApp. O retorno é a resposta gerada pela IA."
    )
    public String simularMensagemRecebida(
            @Parameter(description = "Texto da mensagem enviada pelo cliente", example = "Quero agendar para amanhã", required = true)
            @RequestParam String message,

            @Parameter(description = "Número do robô/empresa que recebe a mensagem", example = "5511999999999", required = true)
            @RequestParam String numberReceived,

            @Parameter(description = "Número do cliente fictício que está enviando a mensagem", example = "5511988888888", required = true)
            @RequestParam String numberSent) {

        return whatsAppMessageService.receiveMessage(message, numberReceived, numberSent);
    }

    @GetMapping("/historic")
    @Operation(
            summary = "Ver histórico do chat simulado",
            description = "Recupera todas as mensagens armazenadas no banco de dados para um número de cliente específico."
    )
    public List<ChatMessage> findChat(
            @Parameter(description = "Número do cliente para buscar o histórico", example = "5511988888888", required = true)
            @RequestParam String numberSent) {

        return whatsAppMessageService.findHistoric(numberSent);
    }
}
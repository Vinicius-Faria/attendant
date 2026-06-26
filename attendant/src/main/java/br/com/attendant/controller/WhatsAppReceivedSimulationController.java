package br.com.attendant.controller;

import br.com.attendant.entity.ChatMessage;
import br.com.attendant.service.WhatsAppMessageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/whatsapp-received-simulation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class WhatsAppReceivedSimulationController {

    private final WhatsAppMessageService whatsAppMessageService;

    public WhatsAppReceivedSimulationController(WhatsAppMessageService whatsAppMessageService) {
        this.whatsAppMessageService = whatsAppMessageService;
    }

    /**
     * @param message Texto da mensagem recebida.
     * @param numberReceived Número de quem recebeu a mensagem.
     * @param numberSent Número de quem enviou a mensagem.
     */
    @GetMapping("/message-received")
    public String carregaVisaoGeralResumoMensal(@RequestParam String message, @RequestParam String numberReceived, @RequestParam String numberSent) {
        return whatsAppMessageService.receiveMessage(message, numberReceived, numberSent);
    }

    @GetMapping("/historic")
    public List<ChatMessage> findChat(@RequestParam String numberSent) {
        return whatsAppMessageService.findHistoric(numberSent);
    }

}

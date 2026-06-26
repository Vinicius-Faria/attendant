package br.com.attendant.controller;

import br.com.attendant.entity.WhatsAppConfig;
import br.com.attendant.service.WhatsAppConfigService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/whatsapp-config", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class WhatsAppConfigController extends BaseController<WhatsAppConfig, Long, WhatsAppConfigService> {

    public WhatsAppConfigController(WhatsAppConfigService whatsAppConfigService) {
        super(whatsAppConfigService);
    }

}

package br.com.attendant.dto;

import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.Enterprise;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContextDto {
    private ChatSession chatSession;
    private List<ChatMessage> chatMessageList;
    private Agenda agenda;
    private Enterprise enterprise;
}

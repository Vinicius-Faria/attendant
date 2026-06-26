package br.com.attendant.repository;

import br.com.attendant.entity.ChatMessage;
import br.com.attendant.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySession(ChatSession chatSession);

}

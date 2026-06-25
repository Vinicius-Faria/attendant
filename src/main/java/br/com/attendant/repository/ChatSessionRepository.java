package br.com.attendant.repository;

import br.com.attendant.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByNumberPhoneClient(String numberPhoneClient);
}

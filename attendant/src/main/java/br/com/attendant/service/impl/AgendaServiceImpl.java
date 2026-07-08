package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.Agenda;
import br.com.attendant.entity.ChatSession;
import br.com.attendant.entity.Enterprise;
import br.com.attendant.entity.SessionStatus;
import br.com.attendant.repository.AgendaRepository;
import br.com.attendant.service.AgendaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
class AgendaServiceImpl extends BaseServiceImpl<Agenda, Long, AgendaRepository> implements AgendaService {

    AgendaServiceImpl(AgendaRepository agendaRepository) {
        super(agendaRepository);
    }

    @Override
    @Transactional
    public Agenda save(Agenda agenda) {
        validate(agenda);

        if (agenda.getChatSession() != null) {
            ChatSession chatSession = agenda.getChatSession();
            if (chatSession.getStatus() != SessionStatus.ACAO_CONCLUIDA) {
                chatSession.setStatus(SessionStatus.ACAO_CONCLUIDA);
            }
        }

        return repository.save(agenda);
    }

    @Override
    public Agenda findByChatSession(ChatSession chatSession) {
        return repository.findByChatSession(chatSession);
    }

    public List<Agenda> findByDateAndEnterprise(LocalDate date, Enterprise enterprise) {
        return repository.findByAtDateHourBetweenAndEnterprise(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX),
                enterprise
        );
    }

    @Override
    public void validate(Agenda entity) {
        if(entity == null){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a entidade.");
        }

        if(entity.getEnterprise() == null ||  entity.getEnterprise().getId() == null){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a empresa.");
        }

        if(entity.getServiceEnterprise() == null ||  entity.getServiceEnterprise().getId() == null){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o serviço a ser agendado.");
        }

        if(entity.getTimeTablesEnterprise() == null ||  entity.getTimeTablesEnterprise().getId() == null){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a agenda da empresa.");
        }

        if(entity.getScheduledFrom() == null ||  entity.getScheduledFrom().isEmpty()){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar o nome do cliente.");
        }

        if(entity.getAtDateHour() == null ||  entity.getAtDateHour().toString().isEmpty()){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a data e hora.");
        }

        if(entity.getIsActive() == null){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar se está ativa.");
        }

    }

}

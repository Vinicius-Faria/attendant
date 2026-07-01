package br.com.attendant.service.impl;

import br.com.attendant.config.BusinessException;
import br.com.attendant.config.ExceptionEnum;
import br.com.attendant.entity.Agenda;
import br.com.attendant.repository.AgendaRepository;
import br.com.attendant.service.AgendaService;
import org.springframework.stereotype.Service;

@Service
class AgendaServiceImpl extends BaseServiceImpl<Agenda, Long, AgendaRepository> implements AgendaService {

    AgendaServiceImpl(AgendaRepository agendaRepository) {
        super(agendaRepository);
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

        if(entity.getChatMessage() == null ||  entity.getChatMessage().getId() == null){
            throw new BusinessException(ExceptionEnum.ENTITY_INCOMPLETE, "É necessário informar a conversa vinculado.");
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

--liquibase formatted sql

--comment: Criacao da tabela de empresa
--changeset viniciusfaria:1
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'enterprise'
CREATE TABLE public.enterprise (
   id SERIAL PRIMARY KEY,
   descricao VARCHAR(100),
   enterprise_type SMALLINT,
   cnpj VARCHAR(20) UNIQUE NOT NULL,
   email VARCHAR(50) UNIQUE NOT NULL,
   senha VARCHAR(255) NOT NULL,
   email_valido BOOLEAN DEFAULT FALSE,
   uuid UUID UNIQUE
);

--comment: Criacao da tabela de WhastApp vinculando a uma empresa
--changeset viniciusfaria:2
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'whatsapp_config'
CREATE TABLE public.whatsapp_config (
   id SERIAL PRIMARY KEY,
   enterprise_id INTEGER NOT NULL,
   number_phone VARCHAR(20),
   wa_business_id VARCHAR(255),
   api_token VARCHAR(255),
   CONSTRAINT fk_whatsapp_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprise (id)
);

--comment: Criacao da tabela de servico vinculado a uma empresa
--changeset viniciusfaria:3
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'service_enterprise'
CREATE TABLE public.service_enterprise (
    id SERIAL PRIMARY KEY,
    enterprise_id INTEGER NOT NULL,
    descricao VARCHAR(20),
    price DOUBLE PRECISION,
    duration INTEGER,
    has_time_between_one_service_and_another BOOLEAN DEFAULT TRUE,
    time_between_one_service_and_another INTEGER DEFAULT 5,
    CONSTRAINT fk_whatsapp_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprise (id)
);

--comment: Criacao da tabela de servico vinculado a uma empresa
--changeset viniciusfaria:4
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'time_tables_enterprise'
CREATE TABLE public.time_tables_enterprise (
   id SERIAL PRIMARY KEY,
   enterprise_id INTEGER NOT NULL,
   day_of_week INTEGER NOT NULL,
   start_time TIME NOT NULL,
   end_time TIME NOT NULL,
   is_closed BOOLEAN DEFAULT FALSE,
   CONSTRAINT fk_whatsapp_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprise (id)
);

--comment: Criacao da tabela de ChatSession vinculado a uma empresa.
--changeset viniciusfaria:5
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'chat_session'
CREATE TABLE public.chat_session (
   id SERIAL PRIMARY KEY,
   enterprise_id INTEGER NOT NULL,
   number_phone_client VARCHAR(20),
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL,
   status VARCHAR(20),
   CONSTRAINT fk_chat_session_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprise (id)
);

--comment: Criacao da tabela de chat_message vinculado a um chat_session.
--changeset viniciusfaria:5
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'chat_message'
CREATE TABLE public.chat_message (
     id SERIAL PRIMARY KEY,
     session_id INTEGER NOT NULL,
     content VARCHAR,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     role VARCHAR(10),
     CONSTRAINT fk_chat_message_enterprise FOREIGN KEY (session_id) REFERENCES public.chat_session (id)
);


--comment: Criacao da tabela de agenda vinculado a service_enterprise_id, timeTablesEnterprise, enterprise,chat_message_id.
--changeset viniciusfaria:6
--preconditions onFail: MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'agenda'
CREATE TABLE public.agenda (
       id SERIAL,
       service_enterprise_id INTEGER NOT NULL,
       time_tables_enterprise_id INTEGER NOT NULL,
       enterprise_id INTEGER NOT NULL,
       chat_session_id INTEGER,
       is_active BOOLEAN DEFAULT FALSE,
       at_date_hour TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       scheduled_for VARCHAR(25),
       CONSTRAINT fk_service_enterprise_enterprise FOREIGN KEY (service_enterprise_id) REFERENCES public.service_enterprise (id),
       CONSTRAINT fk_time_tables_enterprise_enterprise FOREIGN KEY (time_tables_enterprise_id) REFERENCES public.time_tables_enterprise (id),
       CONSTRAINT fk_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprise (id),
       CONSTRAINT fk_chat_session_enterprise FOREIGN KEY (chat_session_id) REFERENCES public.chat_session (id),
       CONSTRAINT uk_agenda_enterprise_date_hour UNIQUE (enterprise_id, at_date_hour)
);


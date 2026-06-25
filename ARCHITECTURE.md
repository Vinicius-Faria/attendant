# Arquitetura em camadas

Este projeto usa uma arquitetura em camadas simples para manter o WhatsApp, o Gemini e o banco desacoplados.

## Camadas

- `controller`: entrada HTTP. Deve depender de interfaces de `service` ou `integration.client`.
- `service`: contratos da aplicacao. Define os casos de uso e servicos disponiveis para outras camadas.
- `service.impl`: implementacoes dos contratos de `service`. Pode acessar `repository` e outros contratos.
- `repository`: contratos Spring Data para persistencia.
- `entity`: entidades JPA persistidas no banco.
- `integration`: contratos e modelos de integracao.
- `integration.client`: contrato de comunicacao com Gemini.
- `integration.client.impl`: implementacao concreta da SDK do Gemini.
- `integration.strategy`: contrato das Function Tools do Gemini.
- `integration.strategy.impl`: implementacoes das Function Tools.
- `integration.registry`: registro das Function Tools disponiveis.

## Regra de dependencias

Nenhuma camada deve depender de uma implementacao concreta de outra camada. Quando um package precisar usar comportamento de outro package, ele deve depender de uma interface.

Exemplos:

- Controllers recebem `WhatsAppMessageService`, nao `WhatsAppMessageServiceImpl`.
- Fluxos de aplicacao recebem `GeminiClient`, nao `GeminiClientImpl`.
- O client do Gemini recebe `GeminiToolRegistry`, nao `GeminiToolRegistryImpl`.
- Novas tools do Gemini implementam `GeminiToolStrategy` e sao registradas automaticamente pelo Spring.

## Como criar uma nova Function Tool do Gemini

1. Crie uma classe em `integration.strategy.impl`.
2. Implemente `GeminiToolStrategy`.
3. Anote com `@Component`.
4. Retorne o nome, schema e execucao da tool.
5. Injete apenas interfaces caso precise consultar dados do sistema.

O `GeminiToolRegistry` encontra todas as strategies automaticamente e entrega as definicoes para o Gemini.

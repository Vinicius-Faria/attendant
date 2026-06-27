# Scratchpad & Dívidas Técnicas

> *Espaço para anotar melhorias e bugs encontrados durante o dev. Limpar antes do Merge Request.*

## Como usar (Configuração no IntelliJ)

Para preencher este arquivo em menos de 3 segundos sem quebrar o seu fluxo de código:
1. Vá em **Settings** (`Ctrl + Alt + S`) -> **Editor** -> **Live Templates**.
2. Clique no **`+`** -> **Live Template**.
3. Defina a abreviação como `todo` e o contexto como **Markdown**.
4. No campo **Template text**, cole exatamente o bloco abaixo:

```text
- [ ] **$TIPO$: $TITULO$**
  - **O que fazer:** $DETALHES$
  - **Task Vinculada:** $TASK$

---
$END$
```

5. Commit: TODO - Titulo da Task
---

- [ ] **Refact: Alterar validação de entidade**
  - **O que fazer:** Usar Jakarta / Hibernate Validator para fazer a validação dentro da entidade.
  - **Task Vinculada:** N/A 

---


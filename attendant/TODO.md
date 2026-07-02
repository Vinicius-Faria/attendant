# Scratchpad & Dívidas Técnicas

> *Espaço para anotar melhorias e bugs encontrados durante o dev. Limpar antes do Merge Request.*

## Como usar (Configuração no IntelliJ)

Para preencher este arquivo em menos de 3 segundos sem quebrar o seu fluxo de código:
1. Vá em **Settings** (`Ctrl + Alt + S`) -> **Editor** -> **Live Templates**.
2. Clique no **`+`** -> **Live Template**.
3. Defina a abreviação como `todo` e o contexto como **Markdown**.
4. No campo **Template text**, cole exatamente o bloco abaixo:

```
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

- [ ] **Refact: Alterar variáveis para ingles**
  - **O que fazer:** Alterar os nomes de algumas variaveis para ingles. Ta uma pizza meia mussarela e meia portuguesa(Luis Eduardo que falou isso kk) 
  - **Task Vinculada:** N/A

---
- [ ] **Feat: Tratar Exception no GeminiClientImpl**
  - **O que fazer:** No arquivo GeminiClientImpl.java dentro da função messageByWhatsApp tem um throw new na exception.
    Porém isso retorna uma exceção para ngm além do back. Se o código chegou até ali, é pq a mensagem veio do whats, então precisa contatar a empresa que deu erro
    ou devolver uma mensagem generica melhor.
  - **Task Vinculada:** N/A

---

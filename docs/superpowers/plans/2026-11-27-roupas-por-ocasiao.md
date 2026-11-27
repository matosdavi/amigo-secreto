# Roupas por ocasião: plano de implementação

> **Para agentes:** SUB-SKILL OBRIGATÓRIO: use superpowers:subagent-driven-development (recomendado) ou superpowers:executing-plans para implementar este plano tarefa por tarefa. Os passos usam checkbox (`- [ ]`) para acompanhamento.

**Objetivo:** O organizador escolhe uma de quatro ocasiões (Natal, Páscoa e chocolate, Trabalho, Festa e aniversário), e a tela de revelação veste a roupa dessa ocasião sobre uma base visual neutra e legível.

**Arquitetura:** A ocasião é um enum guardado em `Comemoracao`, com coluna anulável (nulo = Natal). Ela sai em `GET /api/evento` e muda por `PUT /api/admin/ocasiao`, travada depois do sorteio, como o tema de cor. No frontend, um único `estilo.css` com tokens CSS por `body[data-ocasiao]`. Só `amigo.html` aplica a roupa. A página inicial e o painel ficam na base neutra.

**Tecnologias:** Java 17+ / Spring Boot 4 / JPA, JUnit 5, HTML+CSS+JS puros, Google Fonts (Atkinson Hyperlegible, Fraunces).

**Spec:** `docs/superpowers/specs/2026-11-27-roupas-por-ocasiao-design.md`

## Restrições globais

- Custo R$ 0. Sem framework de frontend e sem imagens: os embrulhos são só CSS.
- Coluna nova **anulável**, porque `ddl-auto=update` roda sobre linhas que já existem em produção.
- Valores de ocasião no JSON, em minúsculas: `natal`, `pascoa`, `trabalho`, `festa`. Padrão: `natal`.
- Base neutra: fundo `#f4f6f8`, texto `#22223b`, linhas `#dce1e7`, texto suave `#5b6472`.
- Letras: Atkinson Hyperlegible (400 e 700) nos textos e Fraunces (700 e 900, opsz 9..144) em títulos e nomes.
- Cores de destaque por ocasião:
  - Natal: `#b3261e`, com verde `#1d6b4f` e dourado `#e0a526`;
  - Páscoa: `#6b3e26`, com rosa `#f2a7b9`;
  - Trabalho: `#2f5bea`, com âmbar `#ffb547`;
  - Festa: `#8e44ec`, com rosa `#ff5c8a`, verde-água `#23c9a9` e amarelo `#ffd23f`.
- Mensagem de demora, exata: "Preparando o amigo secreto… o primeiro acesso pode levar até 1 minuto." Aparece depois de 3 s carregando.
- Respeitar `prefers-reduced-motion`, ter foco visível e funcionar a partir de 320 px.
- Toda a interface em pt-BR.

## Pontos de atenção na revisão

1. Ocasião com maiúsculas ou espaços (`" PASCOA "`) deve ser aceita (sem diferenciar maiúsculas, depois de `trim`). Ausente ou vazia dá 400. Teste na Tarefa 1.
2. Banco antigo com `ocasiao` nula deve aparecer como `natal`, sem erro. Teste na Tarefa 1.
3. Com `prefers-reduced-motion`, abrir o presente ainda mostra o nome, só sem animação. Verificação na Tarefa 3.
4. Nome de 60 letras na revelação, a 320 px de largura, quebra linha sem rolagem horizontal. Verificação na Tarefa 3.
5. Trocar a ocasião não pode desligar o tema de cor nem renomear o evento, e vice-versa. Teste na Tarefa 1.

---

### Tarefa 1: Ocasião no backend

**Arquivos:**
- Criar: `src/main/java/com/natal/amigo_secreto/model/Ocasiao.java`
- Modificar:
  - `model/Comemoracao.java`
  - `service/ComemoracaoService.java`
  - `service/SorteioService.java`
  - `dto/Dtos.java`
  - `controller/AdminController.java`
  - `controller/SorteioController.java`
- Teste: `src/test/java/com/natal/amigo_secreto/service/OcasiaoTest.java`

**Interfaces:**
- Produz:
  - `enum Ocasiao { NATAL, PASCOA, TRABALHO, FESTA }`, com `String valor()` (minúsculas) e `static Ocasiao deTexto(String)`. Esse método lança `RegraException.invalido("Escolha uma ocasião válida: natal, pascoa, trabalho ou festa.")` para texto nulo, vazio ou desconhecido.
  - `Comemoracao`: campo `String ocasiao` (`@Column(length = 20)`, anulável), com `Ocasiao getOcasiao()` (nulo devolve `NATAL`) e `setOcasiao(Ocasiao)`.
  - `ComemoracaoService`: `Ocasiao ocasiao()` e `void definirOcasiao(Ocasiao)`.
  - `SorteioService`: `void mudarOcasiao(String texto)`. Lança 409 com "O sorteio já foi feito. Para mudar a ocasião, desfaça o sorteio primeiro." se já houve sorteio.
  - `Dtos.Evento(String nome, boolean temaCor, String ocasiao)` e `Dtos.NovaOcasiao(String ocasiao)`.
  - `PUT /api/admin/ocasiao`, com corpo `{"ocasiao":"pascoa"}`, devolve 204.

- [ ] **Passo 1: Escrever `OcasiaoTest` (falhando)**, com o mesmo `@SpringBootTest(properties=…)` do `TemaCorTest`. O `@BeforeEach` desfaz o sorteio, apaga os participantes, chama `definirOcasiao(NATAL)` e `definirTemaCor(false)` e renomeia para `""`. Testes:
  - `padraoENatal`: `comemoracao.ocasiao() == NATAL`. Antes, apagar a linha com `ComemoracaoRepository.deleteAll()`.
  - `linhaAntigaComOcasiaoNulaViraNatal`: salvar uma `Comemoracao` sem ocasião e verificar `ocasiao() == NATAL`.
  - `aceitaMaiusculasEEspacos`: `service.mudarOcasiao(" PASCOA ")` resulta em `PASCOA`.
  - `rejeitaInvalidaOuVazia`: `RegraException` para `"carnaval"`, `""` e `null`.
  - `travaDepoisDoSorteio`: com 3 pessoas, sortear e verificar que `mudarOcasiao("festa")` lança exceção e a ocasião continua `NATAL`. Depois de `desfazerSorteio()`, a troca funciona.
  - `independenteDeNomeETemaCor`: definir a ocasião `TRABALHO`, renomear e ligar o tema. A ocasião continua `TRABALHO`, e o nome e o tema continuam como foram definidos.
- [ ] **Passo 2: Rodar e confirmar que falha (não compila)**
  ```bash
  ./mvnw -q test -Dtest=OcasiaoTest
  ```
- [ ] **Passo 3: Implementar as interfaces acima.** `SorteioController.evento()` devolve `new Evento(nome(), temaCor(), ocasiao().valor())`. `AdminController.ocasiao(@RequestBody NovaOcasiao)` chama `service.mudarOcasiao`.
- [ ] **Passo 4: Rodar a suíte inteira e confirmar que passa**
  ```bash
  ./mvnw -q test
  ```
  Todos os testes devem passar, incluindo os 6 novos.
- [ ] **Passo 5: Commit**, com a mensagem "Ocasião da comemoração (natal, páscoa, trabalho, festa) no backend".

### Tarefa 2: Base visual neutra e mensagem de demora

**Arquivos:**
- Modificar:
  - `src/main/resources/static/estilo.css`
  - `src/main/resources/static/evento.js`
  - `src/main/resources/static/index.html`
  - `src/main/resources/static/organizador.html`
  - `src/main/resources/static/amigo.html` (só o `<head>` e o carregando)

**Interfaces:**
- Consome: `GET /api/evento` → `{nome, temaCor, ocasiao}` (Tarefa 1).
- Produz:
  - `Evento.ocasiao` (string, padrão `"natal"`).
  - `Evento.vestir()`, que define `document.body.dataset.ocasiao` e o `meta[name=theme-color]` com a cor de destaque. Só `amigo.html` chama `vestir()`.
  - `Evento.avisarDemora(elemento)`, que retorna uma função de cancelar. Depois de 3000 ms, mostra a mensagem de demora (texto fixado nas restrições globais) dentro do elemento.
  - Tokens CSS: `--fundo`, `--tinta`, `--linha`, `--suave`, `--cartao`, `--destaque`, `--destaque-2`, `--fundo-tinta` (tom do fundo da roupa) e `--confete-1` a `--confete-4`. Os valores da base ficam em `:root`, e os da roupa em `body[data-ocasiao=…]`.

- [ ] **Passo 1: Trocar a fonte Nunito por Atkinson Hyperlegible + Fraunces nas três páginas.** Atkinson vai no `body`. Fraunces vai em `h1`, `.evento`, `.nome-amigo` e `.cor-presente strong`.
- [ ] **Passo 2: Reescrever os tokens de `:root` para a base neutra**, mantendo os nomes de classe atuais. Os botões primários passam a usar `--tinta` (azul-tinta), o verde fica para ações de confirmação e o vermelho sai da base.
- [ ] **Passo 3: Implementar `Evento.vestir()` e `Evento.avisarDemora()`** e ligar o aviso nos carregamentos das três páginas. Na revelação, o aviso aparece no `#carregando`. No painel e na página inicial, aparece num `<p class="suave">` novo abaixo do topo. Cancelar quando a resposta chegar.
- [ ] **Passo 4: Verificar no navegador (preview `amigo-secreto-demo`, 375 px).** Página inicial e painel neutros e legíveis. Simular demora com o throttling do DevTools ou um `setTimeout` no console: a mensagem aparece depois de ~3 s e some quando carrega.
- [ ] **Passo 5: Commit**, com a mensagem "Base visual neutra, letras legíveis e aviso de app acordando".

### Tarefa 3: Revelação vestindo as quatro roupas

**Arquivos:**
- Modificar: `src/main/resources/static/amigo.html` e `src/main/resources/static/estilo.css`

**Interfaces:**
- Consome: `Evento.vestir()` e os tokens (Tarefa 2).
- Produz: `#presente` passa a conter os quatro embrulhos (`.embrulho-natal`, `.embrulho-pascoa`, `.embrulho-trabalho`, `.embrulho-festa`). O CSS mostra só o da ocasião ativa. A classe `.aberto` continua sendo o gatilho da animação.

- [ ] **Passo 1: Marcação dos embrulhos**, seguindo os esboços aprovados (`roupas-por-ocasiao.html` e `roupa-aniversario-v3.html`):
  - **caixas (Natal e Festa):** `.tampa-grupo` (tampa, nó e laço) por cima da `.caixa`;
  - **ovo:** `.ovo-cima` e `.ovo-baixo`;
  - **envelope:** `.envelope-aba` e `.envelope-corpo`.
- [ ] **Passo 2: Animações de abrir.**
  - Caixas: a tampa vai para `translate(-46px,-58px) rotate(-24deg)` e some. Isso é medido para não cobrir o "Olá".
  - Ovo: a metade de cima sobe uns 50 px e gira -20°.
  - Envelope: a aba gira em `rotateX(180deg)` a partir do topo.
  - Com `prefers-reduced-motion`, nenhuma animação, e o nome aparece direto.
- [ ] **Passo 3: O confete usa `--confete-1..4`.** Em `soltarConfete()`, ler as cores com `getComputedStyle(document.body)` no lugar da lista fixa.
- [ ] **Passo 4: Verificar no navegador, a 375 px e a 320 px.**
  - Para cada ocasião (trocar via `PUT /api/admin/ocasiao`): embrulho fechado, abrir, nome e cor sorteada visíveis, botão "Esconder o nome" embaçando o nome e a cor.
  - Nome de 60 letras não cria rolagem horizontal: `document.documentElement.scrollWidth <= innerWidth`.
  - Com `prefers-reduced-motion` emulado, o nome aparece.
  - Tirar um print de cada roupa aberta.
- [ ] **Passo 5: Commit**, com a mensagem "Revelação com roupas: caixa de Natal, ovo, envelope e caixa de festa".

### Tarefa 4: Cartão "Ocasião" no painel

**Arquivos:**
- Modificar: `src/main/resources/static/organizador.html` e `src/main/resources/static/estilo.css`

**Interfaces:**
- Consome: `PUT /api/admin/ocasiao` e `Evento.ocasiao` (Tarefas 1 e 2). Reaproveita `executar()`, `avisar()` e o padrão de trava do `desenharTema()`.

- [ ] **Passo 1: Cartão "Ocasião"** logo depois de "Nome da comemoração".
  - Quatro opções de rádio grandes (mínimo 64 px de altura), cada uma com miniatura CSS do embrulho e o rótulo "Natal", "Páscoa e chocolate", "Trabalho" ou "Festa e aniversário".
  - A miniatura usa as cores da própria ocasião, aplicadas na opção e não na página.
- [ ] **Passo 2: Comportamento.**
  - Ao escolher, chamar `PUT /api/admin/ocasiao` e avisar "Ocasião salva: Páscoa e chocolate."
  - Se der erro, voltar à opção anterior.
  - Depois do sorteio: opções desabilitadas e o aviso "🔒 O sorteio já foi feito. Para mudar a ocasião, desfaça o sorteio primeiro."
- [ ] **Passo 3: Verificar no navegador (375 px).** Escolher cada ocasião e conferir `GET /api/evento`. Sortear e ver a trava. Desfazer e ver que destrava. O painel continua na base neutra, sem tingir.
- [ ] **Passo 4: Commit**, com a mensagem "Painel: escolha da ocasião com miniaturas".

### Tarefa 5: CI, README e entrega

**Arquivos:**
- Modificar: `.github/workflows/ci.yml` e `README.md`

- [ ] **Passo 1: No smoke test do CI**, antes do primeiro sorteio:
  - `curl /api/evento | jq -e '.ocasiao == "natal"'`;
  - `PUT /api/admin/ocasiao {"ocasiao":"pascoa"}` e depois `jq -e '.ocasiao == "pascoa"'`;
  - `{"ocasiao":"carnaval"}` deve dar 400.

  Depois do sorteio, o `PUT` deve dar 409.
- [ ] **Passo 2: No README**, incluir no Guia do organizador o item "Ocasião" (as quatro roupas, travada depois do sorteio) e a linha `PUT /api/admin/ocasiao` na tabela da API.
- [ ] **Passo 3: Rodar a suíte completa e empacotar**
  ```bash
  ./mvnw -q clean package
  ```
  Deve terminar com saída 0 e todos os testes passando.
- [ ] **Passo 4: Commit, push da branch e CI verde**
  ```bash
  gh run watch --exit-status
  ```
  Os 3 jobs devem terminar com sucesso.

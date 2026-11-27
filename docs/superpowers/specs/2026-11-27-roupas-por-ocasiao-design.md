# Roupas por ocasião e base visual neutra: design

## Intenção

O app deixou de ser só o amigo secreto de Natal da família. Ele já é usado em outras situações, como o amigo secreto da tia com as amigas, e os nomes da comemoração mostram outras ocasiões ("Amigo Chocolate", "Time X"). O dono quer um app de amigo secreto **sempre à mão** e bom de compartilhar com pessoas próximas. Para isso, ele precisa ser **fácil para gente mais velha** e **bonito em qualquer ocasião**.

Esta entrega troca o visual 100% natalino por uma **base neutra** com uma **"roupa" por ocasião**, escolhida pelo organizador. A roupa muda o embrulho do presente, as cores e o selo na tela de revelação.

## Objetivos

- O organizador escolhe uma de quatro ocasiões no painel:
  - **Natal:** caixa vermelha com fita e laço dourados.
  - **Páscoa e chocolate:** ovo de chocolate com faixa rosa em zigue-zague.
  - **Trabalho:** envelope azul com selo âmbar.
  - **Festa e aniversário:** caixa roxa de bolinhas, fita e laço amarelos, confete colorido.
- A revelação veste a roupa escolhida: fundo levemente tingido, selo do evento, cor do nome sorteado, embrulho, animação de abrir e cores do confete.
- **Base neutra legível:** fundo claro e frio (`#f4f6f8`), texto azul-tinta (`#22223b`), letra **Atkinson Hyperlegible** nos textos e **Fraunces** em títulos e nomes. O nome sorteado continua sendo o maior elemento da tela.
- **Tampa nova** nas caixas (Natal e Festa): mais larga que a caixa, com aba, sombra, a fita continuando e um laço. Ao abrir, a tampa voa sem cobrir o "Olá, Fulano!".
- **App acordando:** se o carregamento passar de ~3 s, aparece "Preparando o amigo secreto… o primeiro acesso pode levar até 1 minuto." no lugar da rodinha sozinha. Vale para a revelação, o painel e a página inicial.
- O **sorteio de cor** continua funcionando e combina com qualquer roupa.

## Fora do escopo

- Tudo da v2 (issue #1): vários grupos, login com Google, link único no grupo, senha de 4 números.
- Ocasiões além dessas quatro, cores personalizadas e upload de imagens.
- Mudanças no fluxo do participante ou do organizador além do item "Ocasião" no painel.

## O que tornaria isto um fracasso

- Quebrar os links já enviados ou o sorteio da família em andamento.
- Piorar a leitura para quem enxerga pouco: letra menor, contraste baixo ou animação que atrapalha.
- Deixar as páginas pesadas. Os embrulhos são só CSS, sem imagens, e as fontes vêm do Google Fonts, como hoje.

## Restrições

- Custo R$ 0. HTML, CSS e JS puros, sem framework, no Spring Boot atual.
- Banco via `ddl-auto=update`: a coluna nova **precisa aceitar nulo**, porque já existem linhas em produção.
- Deploy manual no Render, nos **dois** serviços (`amigo-secreto` e `amigo-secreto-tia`).
- Os arquivos estáticos já saem com `Cache-Control: no-cache`.
- Respeitar `prefers-reduced-motion`, ter foco visível no teclado e funcionar a partir de 320 px de largura.

## Abordagens consideradas

1. **Roupas agora, no app atual (escolhida).** Entrega o visual novo neste fim de ano com risco baixo: só muda a aparência e acrescenta um campo. A v2 fica para janeiro (issue #1).
2. **v2 completa agora, junto com as roupas.** Grande demais: banco, migração e login do Google mexendo no app durante sorteios em andamento, sem ficar pronta antes de dezembro.

Para implementar as roupas, a escolha foi **um único `estilo.css` com tokens (variáveis CSS) por ocasião, selecionados por `data-ocasiao` no `<body>`**. A alternativa era um CSS por ocasião, que duplicaria a base e tornaria a troca mais lenta.

## Decisões

### Backend
- Enum `Ocasiao { NATAL, PASCOA, TRABALHO, FESTA }`.
- `Comemoracao.ocasiao` é uma coluna `String` que aceita nulo. Nulo é tratado como `NATAL`, o que mantém a aparência do sorteio da família.
- `GET /api/evento` passa a devolver `ocasiao`, além de `nome` e `temaCor`. Em JSON vai em minúsculas: `natal`, `pascoa`, `trabalho`, `festa`.
- `PUT /api/admin/ocasiao` com `{"ocasiao": "pascoa"}`:
  - valor inválido devolve **400**;
  - depois do sorteio devolve **409**, mesma regra do tema de cor. Todos veem a mesma roupa, e "Desfazer sorteio" libera de novo.

### Frontend
- `evento.js` guarda `Evento.ocasiao` e aplica `document.body.dataset.ocasiao`. A página inicial e o painel usam a base neutra, e só a revelação veste a roupa completa.
- O painel ganha o cartão **"Ocasião"** com quatro opções grandes, cada uma com uma miniatura do embrulho. Depois do sorteio as opções ficam travadas com o aviso 🔒, como o tema de cor.
- Na revelação, a marcação de cada embrulho fica pronta no HTML e o CSS mostra a da ocasião ativa. Animação de abrir de cada uma:
  - **caixas:** a tampa voa, como hoje;
  - **ovo:** a metade de cima sobe e gira;
  - **envelope:** a aba abre.
- As cores do confete seguem a ocasião.
- O `meta theme-color` segue a cor de destaque da ocasião.

### Fica a critério de quem implementar
- Ajuste fino de tons e sombras, a partir dos esboços.
- Tempo exato das animações.
- Desenho exato das miniaturas no painel.

## Testes
- Service:
  - ocasião padrão é Natal;
  - trocar a ocasião antes do sorteio funciona;
  - trocar depois do sorteio dá 409;
  - valor inválido dá 400;
  - renomear o evento não muda a ocasião.
- CI ponta a ponta: `GET /api/evento` traz `ocasiao`, e o `PUT` funciona antes do sorteio e é bloqueado depois.
- Navegador, na largura de celular: as quatro roupas fechadas e abertas, esconder o nome, roupa junto com o tema de cor, e o painel com a ocasião travada depois do sorteio.

## Esboços de referência
Ficam em `Desktop/amigo-secreto/.superpowers/brainstorm/` (fora do repositório): `roupas-por-ocasiao.html` e `roupa-aniversario-v3.html` (tampa nova).

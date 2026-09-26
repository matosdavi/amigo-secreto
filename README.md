# 🎁 Amigo Secreto da Família

Aplicação web para organizar o amigo secreto da família **sem papelzinho e sem precisar estar todo mundo no mesmo lugar**.
Cada pessoa recebe pelo WhatsApp um **link só dela**, abre no celular, toca no presente e descobre quem tirou. 🎉

- 👵 **Pensado para todas as idades:** sem cadastro, sem senha, sem aplicativo. Só tocar no link.
- 🔒 **Secreto de verdade:** cada link tem um código aleatório impossível de adivinhar, e **nem o organizador vê quem tirou quem**.
- 🎲 **Sorteio justo:** ninguém tira a si mesmo e o sorteio forma um ciclo único com todos os participantes.
- 📱 **Feito para celular:** letras grandes, botões grandes e uma animação de “abrir o presente”.

## Como funciona

```
Organizador                                 Cada familiar
───────────                                 ─────────────
1. Abre /organizador (com senha)
2. Adiciona os nomes
3. Clica em “Sortear agora”
4. Toca em “WhatsApp” ao lado de  ───────►  5. Recebe a mensagem com o link
   cada nome e envia o link                 6. Abre, toca no presente 🎁
                                            7. Vê quem tirou (e pode esconder o nome)
8. Acompanha quem “já viu” ✅
```

## Tecnologias

| Item | Versão |
|---|---|
| Java | 17 ou superior (testado com 21) |
| Spring Boot | 4.0 (Web MVC + Data JPA) |
| Maven | via Maven Wrapper (`mvnw`), não precisa instalar |
| Banco de dados | H2 em arquivo (padrão, zero instalação) ou PostgreSQL (produção: Neon) |
| Frontend | HTML, CSS e JavaScript puros (sem framework) |

## Executando localmente

Pré-requisito: **JDK 17+** instalado (`java -version` deve mostrar 17 ou mais).

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Abra:

- **http://localhost:8080/organizador** → painel do organizador
- **http://localhost:8080/a/{codigo}** → página de cada participante (os links são gerados no painel)

Na primeira execução, se nenhuma senha for configurada, o console mostra uma **senha temporária do organizador**:

```
Nenhuma ADMIN_SENHA configurada. Senha temporária do organizador: k7m2xq9pwa
```

Os dados ficam salvos na pasta `data/` (banco H2 em arquivo).

### Rodar os testes / gerar o `.jar`

```bash
./mvnw verify                       # compila e roda os testes
java -jar target/amigo-secreto-0.0.1-SNAPSHOT.jar
```

### Configuração (`.env`)

As configurações vêm de variáveis de ambiente ou de um arquivo `.env` na pasta do projeto:

```bash
cp .env.example .env
```

| Variável | Para que serve | Padrão |
|---|---|---|
| `APP_ENV` | `development` ou `production` (em produção, `DATABASE_URL` e `ADMIN_SENHA` são obrigatórios) | `development` |
| `ADMIN_SENHA` | Senha do painel do organizador | gerada e mostrada no console (só em desenvolvimento) |
| `NOME_EVENTO` | Nome exibido nas telas (ex.: `Amigo Pijama`) | `Amigo Secreto` |
| `PORT` | Porta HTTP | `8080` |
| `DATABASE_URL` | Connection string do PostgreSQL (`postgres://usuario:senha@host/banco?sslmode=...`) | vazio = H2 em `./data` |

### PostgreSQL local (opcional)

Para rodar com o mesmo banco da produção, suba o PostgreSQL do `docker-compose.yml` (porta **5433**, para não conflitar com um PostgreSQL instalado no Windows):

```bash
docker compose up -d
```

E no `.env`:

```
DATABASE_URL=postgres://postgres:postgres@localhost:5433/amigo_secreto?sslmode=disable
```

## Disponibilizando para a família (grátis) 🌍

Os links precisam de um **endereço público** para funcionar no celular de quem está em outra casa.

### Produção: Render + Neon (link fixo, recomendado)

O link continua funcionando mesmo com o seu computador desligado.

| Peça | Serviço (plano grátis) | O que configurar |
|---|---|---|
| Banco | [Neon](https://neon.com) (Postgres, região **AWS US East 2 – Ohio**) | copie a connection string para `DATABASE_URL` (ela já vem com `sslmode=require`) |
| Aplicação | [Render](https://render.com) → **Web Service** a partir do `Dockerfile`, região **Ohio** (perto do banco) | as variáveis abaixo e o health check em `/health` |

Variáveis de ambiente no Render (o `Dockerfile` já define `APP_ENV=production` e `PORT`):

```
DATABASE_URL = postgresql://usuario:senha@ep-xxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
ADMIN_SENHA  = uma-senha-forte (mínimo 8 caracteres)
NOME_EVENTO  = Amigo Secreto da Família
```

Em produção a aplicação **se recusa a subir** se faltar `DATABASE_URL` ou `ADMIN_SENHA`, e a mensagem de erro aparece nos logs do Render. Isso evita dois problemas silenciosos: usar o H2 (o disco do plano grátis é apagado a cada reinício) e uma senha temporária que mudaria a cada vez que o servidor acorda.

Depois do deploy, acesse `https://<seu-app>.onrender.com/organizador` e envie os links **a partir desse endereço**.

**⚠️ Servidor dormindo:** no plano grátis do Render, a aplicação "dorme" depois de 15 minutos sem acesso, e o primeiro acesso leva cerca de 1 minuto para acordá-la. Antes de mandar os links, abra o painel você mesmo, e avise a família: *"se demorar um pouquinho para abrir, é normal"*.

**Não use um monitor para manter o servidor sempre acordado.** O Neon também dorme quando ninguém usa, e manter os dois ligados 24 horas por dia estouraria as horas de computação do plano grátis. Cada acesso só gasta enquanto alguém está usando (o pool de conexões solta as conexões ociosas depois de 1 minuto).

### Teste rápido: direto do seu computador com Cloudflare Tunnel

Serve para mostrar para alguém ou testar no celular, sem conta e sem deploy.

1. Instale o `cloudflared` (uma vez só):
   ```powershell
   winget install --id Cloudflare.cloudflared
   ```
2. Rode a aplicação (veja [Executando localmente](#executando-localmente)).
3. Em outro terminal:
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```
4. Ele mostra um endereço como `https://palavras-aleatorias.trycloudflare.com`. Abra **esse endereço + `/organizador`**.

> ⚠️ O computador precisa ficar ligado, e se o túnel for reiniciado o endereço muda (os links antigos param de funcionar).

## CI

O GitHub Actions (`.github/workflows/ci.yml`) roda a cada push:

- **Build e testes:** `./mvnw verify`, incluindo o teste que confere o ciclo do sorteio 50 vezes.
- **Ponta a ponta:** sobe o `.jar` em modo produção contra um PostgreSQL real e faz o fluxo completo (cadastrar, sortear, revelar, senha errada, link inválido).
- **Imagem Docker:** garante que o `Dockerfile` continua compilando.

## Segurança e privacidade

- Cada participante acessa apenas pelo seu link com um código aleatório de 128 bits: não dá para “chutar” o link de outra pessoa.
- O painel do organizador é protegido por senha e **nunca mostra quem tirou quem**, só quem já abriu o link.
- Se um link for enviado para a pessoa errada, use **“Novo link”** no painel: o antigo deixa de funcionar.
- Quem tiver o link de alguém consegue ver o amigo secreto dessa pessoa — por isso a mensagem pede para não compartilhar.

## Estrutura do projeto

```
src/main/java/com/natal/amigo_secreto
├── AmigoSecretoApplication.java   # ponto de entrada
├── config/        # variáveis de ambiente, senha do organizador e rotas das páginas
├── controller/    # API REST: participante (público), admin (com senha) e /health
├── dto/           # records de entrada/saída da API
├── exception/     # erros de negócio → respostas JSON amigáveis
├── model/         # entidade Participante
├── repository/    # acesso ao banco (Spring Data JPA)
└── service/       # regras do sorteio
src/main/resources/static
├── index.html        # página inicial
├── amigo.html        # revelação do amigo secreto (link individual)
├── organizador.html  # painel do organizador
└── estilo.css
```

## API

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/health` | Aplicação e banco respondendo (health check do Render) |
| `GET` | `/api/evento` | Nome do evento |
| `GET` | `/api/participante/{token}` | Nome do participante e se o sorteio já foi feito |
| `POST` | `/api/participante/{token}/revelar` | Revela quem o participante tirou |
| `GET` | `/api/admin` | Lista de participantes e status (🔒) |
| `POST` | `/api/admin/participantes` | Adiciona participante `{"nome": "..."}` (🔒) |
| `DELETE` | `/api/admin/participantes/{id}` | Remove participante (🔒) |
| `POST` | `/api/admin/participantes/{id}/novo-link` | Gera um novo link (🔒) |
| `POST` | `/api/admin/sorteio` | Realiza o sorteio (🔒) |
| `DELETE` | `/api/admin/sorteio` | Desfaz o sorteio (🔒) |

🔒 = exige o cabeçalho `X-Senha-Admin`.

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
| Banco de dados | H2 em arquivo (padrão, zero instalação) ou PostgreSQL |
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

### Configuração (variáveis de ambiente)

| Variável | Para que serve | Padrão |
|---|---|---|
| `ADMIN_SENHA` | Senha do painel do organizador | gerada e mostrada no console |
| `NOME_EVENTO` | Nome exibido nas telas (ex.: `Amigo Pijama`) | `Amigo Secreto` |
| `PORT` | Porta HTTP | `8080` |
| `DB_URL` | URL JDBC do banco | `jdbc:h2:file:./data/amigosecreto` |
| `DB_USER` / `DB_PASSWORD` | Usuário e senha do banco | `sa` / vazio |

Exemplo (PowerShell):

```powershell
$env:ADMIN_SENHA="minha-senha-forte"; $env:NOME_EVENTO="Amigo Pijama"; .\mvnw.cmd spring-boot:run
```

## Disponibilizando para a família (grátis) 🌍

Os links precisam de um **endereço público** para funcionar no celular de quem está em outra casa. Há duas opções gratuitas:

### Opção A — Direto do seu computador com Cloudflare Tunnel (mais rápido)

Ideal para testar ou para quando todos vão abrir o link no mesmo dia. Não precisa de conta.

1. Instale o `cloudflared` (uma vez só):
   ```powershell
   winget install --id Cloudflare.cloudflared
   ```
2. Rode a aplicação (veja [Executando localmente](#executando-localmente)).
3. Em outro terminal:
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```
4. Ele mostra um endereço como `https://palavras-aleatorias.trycloudflare.com`.
   Abra **esse endereço + `/organizador`** e envie os links a partir dali.

> ⚠️ O computador precisa ficar **ligado** com a aplicação e o túnel rodando. Se o túnel for reiniciado, o endereço muda e os links precisam ser reenviados.

### Opção B — Hospedado na nuvem com Render + Neon (link fixo, recomendado para o evento)

O link continua funcionando mesmo com o seu computador desligado.

1. **Banco de dados (Neon, gratuito):** crie uma conta em [neon.tech](https://neon.tech), crie um projeto e copie os dados de conexão.
2. **Aplicação (Render, gratuito):** em [render.com](https://render.com), crie um **Web Service** apontando para este repositório.
   O Render detecta o `Dockerfile` automaticamente. Escolha o plano **Free**.
3. Em **Environment**, configure:
   ```
   ADMIN_SENHA = uma-senha-forte
   NOME_EVENTO = Amigo Secreto da Família
   DB_URL      = jdbc:postgresql://<host-do-neon>/<banco>?sslmode=require
   DB_USER     = <usuario-do-neon>
   DB_PASSWORD = <senha-do-neon>
   ```
4. Após o deploy, acesse `https://<seu-app>.onrender.com/organizador`.

> ℹ️ No plano gratuito do Render a aplicação “dorme” após ~15 minutos sem uso; o primeiro acesso depois disso pode levar cerca de 1 minuto. Vale avisar a família: *“se demorar um pouquinho para abrir, é normal”*.
>
> ⚠️ Não use o banco H2 no Render: o disco do plano gratuito é apagado a cada reinício. Por isso o Neon (PostgreSQL).

## Segurança e privacidade

- Cada participante acessa apenas pelo seu link com um código aleatório de 128 bits: não dá para “chutar” o link de outra pessoa.
- O painel do organizador é protegido por senha e **nunca mostra quem tirou quem**, só quem já abriu o link.
- Se um link for enviado para a pessoa errada, use **“Novo link”** no painel: o antigo deixa de funcionar.
- Quem tiver o link de alguém consegue ver o amigo secreto dessa pessoa — por isso a mensagem pede para não compartilhar.

## Estrutura do projeto

```
src/main/java/com/natal/amigo_secreto
├── AmigoSecretoApplication.java   # ponto de entrada
├── config/        # senha do organizador (interceptor) e rotas das páginas
├── controller/    # API REST: participante (público) e admin (com senha)
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

// Nome da comemoração (escolhido pelo organizador), usado em todas as páginas.
// Qualquer que seja o nome ("Amigo Chocolate", "Amigo Secreto Time X"...),
// a tela deixa claro que se trata de um amigo secreto.

const Evento = {
    nome: "Amigo Secreto",

    /** Tema "Amigo de Uma Cor Só" ligado pelo organizador. */
    temaCor: false,

    /** Roupa visual: "natal", "pascoa", "trabalho" ou "festa". */
    ocasiao: "natal",

    /** Veste a roupa da ocasião na página (só a revelação usa). */
    vestir() {
        document.body.dataset.ocasiao = Evento.ocasiao;
        const cor = getComputedStyle(document.body).getPropertyValue("--destaque").trim();
        const meta = document.querySelector('meta[name="theme-color"]');
        if (meta && cor) meta.content = cor;
    },

    /**
     * Se o carregamento passar de 3 s (o servidor gratuito "dorme"), explica a espera.
     * Devolve a função que cancela/esconde o aviso.
     */
    avisarDemora(elemento) {
        const timer = setTimeout(() => {
            elemento.textContent = "Preparando o amigo secreto… o primeiro acesso pode levar até 1 minuto.";
            elemento.classList.remove("oculto");
        }, 3000);
        return () => {
            clearTimeout(timer);
            elemento.classList.add("oculto");
        };
    },

    /** true se o próprio nome já diz "amigo secreto" (ignorando maiúsculas e acentos). */
    jaDizAmigoSecreto(nome) {
        const normalizado = nome.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
        return normalizado.includes("amigo secreto");
    },

    /** Ex.: "Amigo Chocolate (amigo secreto)" ou só "Amigo Secreto da Família". */
    descricao(nome = Evento.nome) {
        return Evento.jaDizAmigoSecreto(nome) ? nome : `${nome} (amigo secreto)`;
    },

    /** Preenche o topo da página: #evento com o nome e #evento-sub com o lembrete. */
    mostrar(nome) {
        Evento.nome = nome;
        document.getElementById("evento").textContent = nome;
        document.getElementById("evento-sub").classList.toggle("oculto", Evento.jaDizAmigoSecreto(nome));
        document.title = nome + " 🎁";
    },

    async carregar() {
        try {
            const resposta = await fetch("/api/evento");
            if (resposta.ok) {
                const dados = await resposta.json();
                Evento.temaCor = dados.temaCor === true;
                // Só as ocasiões que o CSS conhece; qualquer outra deixaria o presente invisível.
                Evento.ocasiao = ["natal", "pascoa", "trabalho", "festa"].includes(dados.ocasiao)
                    ? dados.ocasiao : "natal";
                Evento.mostrar(dados.nome);
            }
        } catch { /* mantém o nome padrão */ }
        return Evento.nome;
    },
};


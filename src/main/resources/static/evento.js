// Nome da comemoração (escolhido pelo organizador), usado em todas as páginas.
// Qualquer que seja o nome ("Amigo Chocolate", "Amigo Secreto Time X"...),
// a tela deixa claro que se trata de um amigo secreto.

const Evento = {
    nome: "Amigo Secreto",

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
            if (resposta.ok) Evento.mostrar((await resposta.json()).nome);
        } catch { /* mantém o nome padrão */ }
        return Evento.nome;
    },
};

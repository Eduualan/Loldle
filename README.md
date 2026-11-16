# LOLDLE

Aplicativo Android para adivinhar campeões de League of Legends. A cada palpite, o jogador recebe dicas de classe, gênero, raça/espécie, recurso e alcance. Os retratos aparecem nas sugestões e listas. Ao acertar, a vitória é salva no histórico e uma nova rodada começa.

## Funcionalidades

- Login de jogador e perfil administrativo de demonstração (PIN `1234`).
- Rodadas de até seis tentativas, com sugestões de campeões e dicas.
- Nova rodada automática após acertar; o botão **Nova partida** também reinicia o jogo.
- Histórico local e ranking de vitórias.
- Consulta, cadastro, edição e exclusão de campeões, incluindo classe, recurso, alcance, gênero e espécie.
- Consulta, edição e exclusão de partidas no perfil administrativo.
- Sincronização de nomes, classes, recursos e alcance com a API [Data Dragon](https://developer.riotgames.com/docs/lol#data-dragon); retratos carregados do CDN oficial.
- Gênero e raça/espécie complementados pelo arquivo comunitário [LoLdleData](https://github.com/iEranDEV/loldle-data/blob/master/output.json), pois esses campos não fazem parte do catálogo do Data Dragon.
- Uma lista de demonstração é usada se a API estiver indisponível na primeira inicialização.

## Tecnologias e arquitetura

- Kotlin e Jetpack Compose para interface.
- MVVM com `GameViewModel`, `AdminViewModel` e `SessionViewModel`, criados por `AppViewModelFactory`.
- Room para persistência local de campeões e partidas, com listas reativas via `Flow`.
- Retrofit para integração com Data Dragon e LoLdleData.
- Navigation Compose e barra de navegação inferior.

Os pacotes principais ficam em `data/local`, `data/remote`, `data/repository` e `ui`.

## Executar

1. Abra o projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Selecione um emulador ou dispositivo Android e execute a configuração `app`.

O projeto usa `minSdk 24`. O Android Studio instalará as dependências declaradas nos arquivos Gradle durante a sincronização.

## Observações

- O PIN de administrador é fixo e serve apenas para demonstração acadêmica; não é autenticação segura para produção.
- O catálogo base vem em inglês (`en_US`); os campos de gênero e espécie são traduzidos pelo app quando há tradução disponível.
- As partidas são armazenadas localmente no dispositivo.

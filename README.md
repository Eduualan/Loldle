# LOLDLE

Aplicativo Android de adivinhação de campeões de League of Legends. O jogador escolhe um nome entre as sugestões e recebe dicas de classe, recurso e alcance. Ao acertar, a vitória é salva no histórico e uma nova rodada começa.

## Funcionalidades

- Login de jogador e perfil administrativo de demonstração (PIN `1234`).
- Rodadas de até seis tentativas, com sugestões de campeões e dicas.
- Nova rodada automática após acertar; o botão **Nova partida** também reinicia o jogo.
- Histórico local e ranking de vitórias.
- Consulta, cadastro, edição e exclusão de campeões.
- Consulta, edição e exclusão de partidas no perfil administrativo.
- Sincronização de campeões com a API Data Dragon; uma lista de demonstração é usada se a rede estiver indisponível durante a primeira inicialização.

## Tecnologias e arquitetura

- Kotlin e Jetpack Compose para interface.
- MVVM com `GameViewModel`, `AdminViewModel` e `SessionViewModel`, criados por `AppViewModelFactory`.
- Room para persistência local de campeões e partidas, com listas reativas via `Flow`.
- Retrofit para integração com Data Dragon.
- Navigation Compose e barra de navegação inferior.

Os pacotes principais ficam em `data/local`, `data/remote`, `data/repository` e `ui`.

## Executar

1. Abra o projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Selecione um emulador ou dispositivo Android e execute a configuração `app`.

O projeto usa `minSdk 24`. O Android Studio instalará as dependências declaradas nos arquivos Gradle durante a sincronização.

## Observações

- O PIN de administrador é fixo e serve apenas para demonstração acadêmica; não é autenticação segura para produção.
- O catálogo remoto vem em inglês (Data Dragon, `en_US`); a interface do aplicativo está em português.
- As partidas são armazenadas localmente no dispositivo.

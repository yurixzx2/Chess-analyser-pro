# TAL PRO V3 - Chess Analyzer

Este é um projeto Android completo para o Chess Analyzer "TAL PRO V3", projetado para ser compilado no AndroidIDE.

## Estrutura do Projeto

- **Package:** `com.example.chessanalyzer`
- **compileSdk:** 34
- **minSdk:** 26
- **targetSdk:** 34
- **Linguagem:** Kotlin
- **Build system:** Gradle (Kotlin DSL)

## Funcionalidades Principais

- **MainActivity:** Painel de configurações com slider de Depth, toggles para diversas opções (Show Arrows, Show Enemy Best Move, Hide Suggestions, Analyze My Moves, Show Evaluation, Move Notation, Endgame Boost, Auto Play), seletor de Style Mode e botão START.
- **Overlay em jogo:** Barra de avaliação, badge de classificação de lance, canvas para setas no tabuleiro e informações compactas.
- **AccessibilityService:** Para ler o tabuleiro do Chess.com e Lichess.
- **Engine - Stockfish:** Wrapper para executar o binário nativo `libstockfish.so` (não incluído no projeto, deve ser adicionado manualmente).
- **Engine - Maia:** Implementação baseada no código-fonte oficial, utilizando modelo TFLite (`maia_1500.tflite`, não incluído no projeto, deve ser adicionado manualmente) para prever lances humanos.
- **Estilos de Jogo:** Default, Aggressive, Defensive, Trader, Human, Crazy.
- **Classificação de Lances:** Baseada na diferença de centipawns.

## Instruções de Compilação no AndroidIDE

Para compilar e executar este projeto no AndroidIDE, siga os passos abaixo:

1.  **Clone ou Baixe o Projeto:**
    Obtenha o código-fonte deste projeto e descompacte-o em um diretório de sua escolha.

2.  **Abra no AndroidIDE:**
    No AndroidIDE, selecione a opção para abrir um projeto existente e navegue até o diretório raiz do projeto `TalProV3`.

3.  **Adicione os Binários das Engines:**
    *   **Stockfish:** O binário `libstockfish.so` não está incluído neste repositório. Você precisará obter uma versão compilada do Stockfish para Android (geralmente para arquiteturas `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`) e colocá-lo na pasta `app/src/main/jniLibs/<architecture>/` (ex: `app/src/main/jniLibs/arm64-v8a/libstockfish.so`).
    *   **Maia Engine:** O modelo TFLite `maia_1500.tflite` não está incluído. Baixe o modelo e coloque-o na pasta `app/src/main/assets/`.

4.  **Sincronize o Projeto Gradle:**
    Após abrir o projeto e adicionar os binários, o AndroidIDE deve solicitar a sincronização do Gradle. Confirme a sincronização para que todas as dependências sejam baixadas e o projeto seja configurado corretamente.

5.  **Construa e Execute:**
    Uma vez que o Gradle tenha sincronizado com sucesso, você pode construir o projeto e executá-lo em um emulador ou dispositivo Android conectado.

## Requisitos

-   AndroidIDE instalado e configurado.
-   Binário do Stockfish compilado para Android.
-   Modelo TFLite do Maia Engine (`maia_1500.tflite`).

## Observações

-   O `AccessibilityService` requer permissão manual do usuário nas configurações de acessibilidade do dispositivo para funcionar corretamente.
-   A funcionalidade de overlay e leitura do tabuleiro é um placeholder e precisará de implementação detalhada para interagir com os elementos específicos das interfaces do Chess.com e Lichess via `AccessibilityNodeInfo`.

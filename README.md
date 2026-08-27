# Cielo Ingressos

Aplicativo Android para consultar eventos, selecionar ingressos e acompanhar o total do pedido.

## Tecnologias

- Kotlin e Gradle Kotlin DSL
- Android SDK, AppCompat e Fragment
- Jetpack Compose e Material 3 para a interface
- ViewModel e StateFlow para estado de tela
- JUnit 4 para testes unitários
- Compose UI Test para testes instrumentados

## Arquitetura e padrões

- Camadas `data`, `domain` e `presentation` para separar leitura de dados, regras de negócio e interface.
- Repository (`EventRepository`) para desacoplar a origem dos eventos da tela.
- Use case (`CalculateOrderTotal`) para concentrar o cálculo do pedido.
- MVVM: `TicketViewModel` expõe um `TicketUiState` imutável via `StateFlow`.
- Componentização Compose: header, card de evento, seletor de quantidade e resumo do pedido são componentes independentes e reutilizáveis.

## Estratégias adotadas

- Compose BOM para manter versões compatíveis das bibliotecas Compose.
- Design system local baseado nas cores Cielo e em `MaterialTheme`.
- Edge-to-edge com o header preenchendo a região da status bar e ícones claros.
- Estado unidirecional: ações da interface chamam o ViewModel, que produz um novo estado para a tela.
- Testes cobrem cálculo de totais, limites de quantidade e interação com o seletor de ingressos.

## Validação

```bash
./gradlew assembleDebug testDebugUnitTest connectedDebugAndroidTest
```

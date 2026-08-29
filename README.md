 <img src="SellCieIcon.png" width="100" alt="Logo">

# SellCie  

## 🎫  Solução Integrada de Venda de Ingressos

O **SellCie** é um ponto de venda (PDV) móvel completo, projetado para operar de forma offline e integrada aos terminais **Cielo Smart**. Ele permite a produtores de eventos gerenciar catálogos, realizar vendas seguras e entregar ingressos digitais com QR Code instantaneamente.

<img src="Cielo Ingressos.png" width="100" alt="Logo"> <img src="Ver meus ingressos.png" width="100" alt="Logo"> <img src="Ver meus ingressos end.png" width="100" alt="Logo">

 <img src="Tela de controle de estoque e cadastro de eventos.png" width="100" alt="Logo"> <img src="Fragment de add e sub de ingressos dos eventos.png" width="100" alt="Logo"> <img src="Fragment de add e sub de ingressos dos eventos end.png" width="100" alt="Logo">

---

## 🏗 Decisões Arquiteturais

A solução foi estruturada seguindo os princípios de **Clean Architecture** e o padrão **MVVM (Model-View-ViewModel)**, garantindo manutenibilidade, testabilidade e desacoplamento.

- **Presentation Layer**: Implementada inteiramente em **Jetpack Compose**, utilizando o padrão *State Hoisting* e gerenciamento de estado via `StateFlow`.
- **Domain Layer**: Contém a lógica de negócio pura (Use Cases), isolada de detalhes técnicos como banco de dados ou APIs externas.
- **Data Layer**: Utiliza o **Repository Pattern** para abstrair as fontes de dados. A persistência é realizada em um banco de dados local, e os pagamentos são mediados por adaptadores de gateway.
- **Offline-First**: O app prioriza o funcionamento sem rede, garantindo que o fluxo de checkout e o histórico de compras estejam sempre disponíveis.

---

## 📚 Bibliotecas Externas e Justificativas

- **Gson (Google)**: Utilizada para a serialização e desserialização robusta dos payloads JSON exigidos pela Cielo. Escolhida pela estabilidade e facilidade de integração.
- **g0dkar.qrcode**: Biblioteca leve e eficiente para geração de QR Codes em Kotlin. Essencial para a entrega do ingresso digital no formato MVP.
- **Jetpack Compose Material 3**: Design system moderno do Google que permite criar UIs adaptativas e acessíveis com menos código.
- **Coroutines & Flow**: Utilizados para operações assíncronas e processamento reativo de dados, garantindo uma UI fluida e sem travamentos.
  
<img src="Ver meus ingressos.png" width="100" alt="Logo"> <img src="Ver meus ingressos end.png" width="100" alt="Logo">
---

## 📲 Integração com Cielo Smart

A integração foi realizada através do protocolo de **Deep Link**, seguindo os padrões oficiais da Cielo:

1.  **Request**: O SellCie gera um objeto `OrderRequest`, converte para JSON via Gson e o codifica em **Base64**.
2.  **Disparo**: Uma Intent com a URI `lio://payment?request=<BASE64>&urlCallback=sellcie://payment-result` é enviada ao sistema.
3.  **Callback**: O app escuta o retorno em uma Activity dedicada (`PaymentResponseActivity`), que extrai o resultado (Sucesso, Erro ou Cancelamento).
4.  **Sandbox**: O sistema mapeia os **Magic Values** (valores em centavos) para simular comportamentos específicos do terminal em ambiente de teste.

<img src="Confirme sua compra.png" width="100" alt="Logo"> <img src="Tela de sucesso.png" width="100" alt="Logo"> <img src="Tela de cancelado pelo user.png" width="100" alt="Logo"> <img src="Tela de erro.png" width="100" alt="Logo">
---

## ⚖️ Trade-offs Considerados

- **SQLite Nativo vs Room**: Optou-se pelo uso de `SQLiteOpenHelper` nativo para demonstrar domínio sobre os fundamentos de persistência do Android e manter a camada de dados leve, embora o Room fosse uma alternativa mais abstrata.
- **Gateway Abstrato**: Criamos uma interface `PaymentGateway` que permite alternar facilmente entre o emulador da Cielo e um gateway mock (offline), facilitando o desenvolvimento sem depender de hardware físico.
- **Comunicação por Resursos**: Refatoramos o envio de mensagens do ViewModel para a UI usando o padrão `UiText`. Isso evita o vazamento de instâncias de `Context` e respeita o ciclo de vida do Compose, em troca de uma leve complexidade inicial.

---

## 🚀 Instruções de Execução

### Pré-requisitos
- Android Studio Ladybug ou superior.
- Android SDK 36 (API Level 36).
- Java 11+.

### Configuração de Credenciais
1. Localize o arquivo `cielo.local.properties.example` na raiz do projeto.
2. Renomeie-o para `cielo.local.properties`.
3. Preencha seu `CIELO_CLIENT_ID` e `CIELO_ACCESS_TOKEN` (ou deixe em branco para usar os valores dummy do emulador).

### Comandos de Build
Para gerar o APK compatível com o emulador:
```bash
./gradlew assembleCieloEmulator
```

Para rodar os testes unitários:
```bash
./gradlew testDebugUnitTest
```

---

## 🏁 Conclusão do MVP
O SellCie entrega uma base sólida para um ecossistema de eventos. Com foco em **segurança**, **padronização profissional** (eliminação de magic numbers) e **excelência em UX** (scroll, mensagens claras e feedbacks técnicos), o projeto está pronto para evoluir para uma solução de larga escala.

**Desenvolvido por**: Marlena Martins (com auxílio da IA Gemini 2.0 Flash)

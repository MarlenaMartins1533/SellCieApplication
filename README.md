# Cielo Ingressos

Aplicativo Android offline para catálogo de eventos, seleção de ingressos, pagamento determinístico local e comprovante persistido no dispositivo.

## Identidade visual

O aplicativo é exibido como **SellCie**. O launcher icon usa um ticket azul com marca de confirmação, criado para o projeto e distribuído em PNG nas densidades Android (`mdpi` a `xxxhdpi`). A geração foi feita localmente com uma ferramenta de IA, sem texto, marcas de terceiros ou dados de pessoas.

## Tecnologias

- Kotlin, Android SDK, AppCompat/Fragment e Jetpack Compose Material 3
- MVVM com ViewModel, StateFlow, Repository e casos de uso
- SQLiteOpenHelper nativo para a tabela local `purchase_attempt`
- JUnit 4 e Compose UI Test; testes instrumentados de persistência

## Fluxo e arquitetura

O fluxo é catálogo → seleção → confirmação da compra → processamento → comprovante. A confirmação exibe quantidade e total antes de iniciar o pagamento, permitindo voltar ao catálogo sem criar uma tentativa. O gateway padrão é `LocalPaymentGateway`, sem rede, credenciais ou transação externa. `ProcessPayment` cria/consulta uma tentativa pelo `purchaseId`; uma tentativa finalizada nunca chama o gateway novamente. Resultados aprovados, negados, cancelados e erros técnicos são persistidos localmente.

Em um comprovante aprovado, a ação **Ver meus ingressos** abre uma lista de cartões individuais: cada unidade comprada recebe nome, numeração e um campo próprio reservado ao QR Code. O identificador do campo é determinístico por compra, evento e unidade; a geração de QR Code escaneável segue fora do escopo offline atual. Ao voltar ao catálogo depois de uma compra aprovada, o carrinho é limpo. Em resultados não aprovados, a seleção é preservada para uma nova tentativa.

O adaptador Cielo real permanece fora do build porque o SDK/emulador e sua documentação compatível não foram confirmados localmente. Ele pode ser conectado atrás de `PaymentGateway` sem alterar a UI ou as regras de idempotência.

## Execução local

Abra o projeto no Android Studio com SDK 36 disponível e execute a variante `debug`. Não é necessário configurar credencial nem conexão de rede.

## Validação

```bash
./gradlew --offline assembleDebug testDebugUnitTest connectedDebugAndroidTest
```

Os testes instrumentados cobrem persistência, confirmação explícita, comprovante itemizado e a lista de ingressos individuais. Eles também validam os estados finais, recuperação por `purchaseId` e reentrada sem nova cobrança.

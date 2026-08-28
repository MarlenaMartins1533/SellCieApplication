# Cielo Ingressos

Aplicativo Android offline para catálogo de eventos, seleção de ingressos, pagamento determinístico local e comprovante persistido no dispositivo.

## Tecnologias

- Kotlin, Android SDK, AppCompat/Fragment e Jetpack Compose Material 3
- MVVM com ViewModel, StateFlow, Repository e casos de uso
- SQLiteOpenHelper nativo para a tabela local `purchase_attempt`
- JUnit 4 e Compose UI Test; testes instrumentados de persistência

## Fluxo e arquitetura

O fluxo é catálogo → seleção → checkout → comprovante. O gateway padrão é `LocalPaymentGateway`, sem rede, credenciais ou transação externa. `ProcessPayment` cria/consulta uma tentativa pelo `purchaseId`; uma tentativa finalizada nunca chama o gateway novamente. Resultados aprovados, negados, cancelados e erros técnicos são persistidos localmente.

O adaptador Cielo real permanece fora do build porque o SDK/emulador e sua documentação compatível não foram confirmados localmente. Ele pode ser conectado atrás de `PaymentGateway` sem alterar a UI ou as regras de idempotência.

## Execução local

Abra o projeto no Android Studio com SDK 36 disponível e execute a variante `debug`. Não é necessário configurar credencial nem conexão de rede.

## Validação

```bash
./gradlew --offline assembleDebug testDebugUnitTest connectedDebugAndroidTest
```

O teste instrumentado de persistência usa o banco local e valida todos os status finais, recuperação por `purchaseId` e reentrada sem nova cobrança.

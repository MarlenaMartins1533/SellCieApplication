# Cielo Ingressos

Aplicativo Android offline para catálogo de eventos, seleção de ingressos, pagamento determinístico local e comprovante persistido no dispositivo.

## Identidade visual

O aplicativo é exibido como **SellCie**. O launcher icon usa um ticket azul com marca de confirmação, criado para o projeto e distribuído em PNG nas densidades Android (`mdpi` a `xxxhdpi`). A geração foi feita localmente com uma ferramenta de IA, sem texto, marcas de terceiros ou dados de pessoas.

## Tecnologias utilizadas

- Kotlin 2.2.21, Android Gradle Plugin 9.1.1, Android SDK 36 e Java 11
- AppCompat, Fragment KTX e Jetpack Compose Material 3
- Arquitetura MVVM com `ViewModel`, `StateFlow`, Repository e casos de uso
- `SQLiteOpenHelper` nativo para persistência local de tentativas e itens da compra
- Deep Link Android via `Intent.ACTION_VIEW` para a integração Cielo Smart
- Base64 UTF-8 para transporte do payload e retorno da Cielo
- Foreground Service para manter a sessão de pagamento visível enquanto aguarda callback
- JUnit 4, testes unitários e Compose UI Test instrumentado

## Prompts utilizados

Os prompts de produto e implementação utilizados no desenvolvimento estão preservados em `documentacao-local`:

- [T4 — Integrações remotas controladas](documentacao-local/tasks/T4.md)
- [T5 — Integração local com Emulador Cielo via Deep Link](documentacao-local/tasks/T5-integracao-emulador-cielo.md)
- `Registro_Prompts_IA_App_Ingressos.docx`, com o histórico de prompts do aplicativo

O prompt da T5 orientou a criação da variante `cieloEmulator`, o contrato `lio://payment`, o callback `sellcie://payment-result`, a persistência idempotente por `purchaseId`, o tratamento seguro de credenciais e a separação entre gateway local e Cielo.

## Fluxo e arquitetura

O fluxo é catálogo → seleção → confirmação da compra → processamento → comprovante. A confirmação exibe quantidade e total antes de iniciar o pagamento, permitindo voltar ao catálogo sem criar uma tentativa. O gateway padrão é `LocalPaymentGateway`, sem rede, credenciais ou transação externa. `ProcessPayment` cria/consulta uma tentativa pelo `purchaseId`; uma tentativa finalizada nunca chama o gateway novamente. Resultados aprovados, negados, cancelados e erros técnicos são persistidos localmente.

Em um comprovante aprovado, a ação **Ver meus ingressos** abre uma lista de cartões individuais: cada unidade comprada recebe nome, numeração e um campo próprio reservado ao QR Code. O identificador do campo é determinístico por compra, evento e unidade; a geração de QR Code escaneável segue fora do escopo offline atual. Ao voltar ao catálogo depois de uma compra aprovada, o carrinho é limpo. Em resultados não aprovados, a seleção é preservada para uma nova tentativa.

Na variante `debug`, o gateway continua local e determinístico. Na variante `cieloEmulator`, `CieloPaymentGateway` implementa `PaymentGateway`, abre o app Cielo por deep link e conclui a tentativa somente após o callback validado. O pacote documentado é `com.ads.lio.uriappclient`; no dispositivo físico usado para validação foi encontrado também `br.com.cielosmart.orderservice`, por isso ambos têm visibilidade declarada no Manifest.

O fluxo aprovado é: persistir tentativa como `PROCESSING` → abrir a Cielo → receber e validar callback → concluir a tentativa → abater estoque → exibir comprovante → abrir os ingressos comprados. A baixa de estoque ocorre somente para `Approved`, e o carrinho é preservado em cancelamentos e erros.

## Execução local

Abra o projeto no Android Studio com SDK 36 disponível.

Para o fluxo offline, execute a variante `debug`; não é necessário configurar credencial nem conexão de rede.

Para testar o emulador Cielo, crie `cielo.local.properties` na raiz a partir de [cielo.local.properties.example](cielo.local.properties.example), preencha `CIELO_CLIENT_ID` e `CIELO_ACCESS_TOKEN` somente no ambiente local autorizado e execute a variante `cieloEmulator`:

```bash
./gradlew assembleCieloEmulator
```

`cielo.local.properties` é ignorado pelo Git. As credenciais não devem aparecer em código, logs, testes, documentação ou artefatos de produção.

## Validação

```bash
./gradlew testDebugUnitTest assembleDebug assembleCieloEmulator
```

Os testes unitários cobrem persistência/idempotência, montagem do payload, Base64, validação do callback e mapeamento dos resultados. Os testes instrumentados/UI cobrem confirmação explícita, persistência, comprovante itemizado e ingressos individuais. A validação real da transação Cielo exige credenciais locais preenchidas, o app Cielo instalado e seleção manual de sucesso, cancelamento ou erro no emulador.

O campo apresentado na tela de ingressos atualmente exibe o payload determinístico reservado para o QR Code; a geração de uma imagem QR escaneável ainda não faz parte desta entrega offline.

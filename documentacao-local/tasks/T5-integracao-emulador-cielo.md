# T5 — Integração local com Emulador Cielo via Deep Link

## Prompt de implementação

Você é especialista em Android Kotlin, Cielo Smart, Deep Links, segurança de pagamentos e testes. Implemente a integração local do app SellCie com o Emulador Cielo, seguindo exclusivamente o fluxo Deep Link da documentação oficial da Cielo Smart. Preserve integralmente o comportamento validado das tarefas T1–T4: catálogo → confirmação explícita → processamento → comprovante → ingressos, e mantenha a regra de idempotência por `purchaseId`.

### Contexto obrigatório

- O emulador Cielo é instalado no mesmo Android/AVD do app e deve ser testado em Android 7.1 ou Android 10.
- A chamada de pagamento é local, por Intent: `lio://payment?request=<base64-json>&urlCallback=<callback-do-app>`.
- A Cielo devolve o resultado chamando a URI registrada pelo SellCie. O parâmetro `response` é Base64 e contém JSON de sucesso ou erro.
- A credencial de desenvolvimento será inserida manualmente apenas em `cielo.local.properties`, na raiz do projeto. Esse arquivo é ignorado pelo Git. Use as propriedades `CIELO_CLIENT_ID` e `CIELO_ACCESS_TOKEN`; nunca insira valores no código, em testes, logs, README, artefatos de release ou histórico Git.
- Se qualquer credencial estiver vazia, não inicie o pagamento Cielo: apresente uma falha segura, sem revelar qual segredo está ausente.

### Implementação exigida

1. Adicione uma variante/configuração de desenvolvimento para emulador sem substituir o gateway offline padrão nas demais execuções.
2. Configure o `AndroidManifest.xml` com:
   - visibilidade do pacote `com.ads.lio.uriappclient` em `<queries>`;
   - metadata `cs_integration_type` com valor `uri`;
   - activity exportada exclusivamente para o callback, com `ACTION_VIEW`, categoria `DEFAULT` e esquema/host próprios do SellCie, por exemplo `sellcie://payment-result`.
3. Crie um adaptador Cielo atrás de `PaymentGateway`; ele deve converter `PaymentRequest` para o JSON aceito pela Cielo:
   - `clientID` e `accessToken` do arquivo local;
   - `reference` igual ao `purchaseId`;
   - todos os `PurchasedTicket` em `items`;
   - valor total e preços unitários em centavos, sem `Double`;
   - `installments = 0`;
   - não enviar e-mail ou dados pessoais que o app não coleta.
4. Como o resultado é assíncrono, não finja que `PaymentGateway.process()` retorna uma aprovação imediata. Modele explicitamente as etapas iniciar → aguardando retorno → concluir callback, preservando a tentativa `PROCESSING` no banco até o callback final.
5. Antes de abrir o Deep Link, persista a tentativa. Monte a URI com JSON em Base64 e dispare `Intent.ACTION_VIEW`. Trate a ausência do emulador/app Cielo de forma segura e conclua a tentativa como erro técnico, sem expor detalhes técnicos na UI.
6. No callback, valide scheme, host, presença de `response`, Base64 e JSON antes de atualizar o banco. Mapeie sucesso para `Approved`; cancelamento informado pela Cielo para `Canceled`; erros de pagamento, autenticação, URI inválida ou JSON inválido para `TechnicalError`. Não tome campos retornados pelo Deep Link como fonte para alterar preço, itens ou `purchaseId` local.
7. Mantenha um foreground service somente enquanto a sessão de pagamento estiver pendente e pare-o sempre que o callback, erro de lançamento ou cancelamento terminar o fluxo. Use uma notificação clara e mínima.
8. Preserve a UI: o comprovante só é exibido após o callback final; ingresso e baixa de estoque só ocorrem em `Approved`; cancelamento/erro preservam o carrinho e permitem uma nova tentativa segura.

### Validação exigida

- Testes unitários para montagem do payload, codificação Base64, validação/decodificação do callback e mapeamento de todos os resultados.
- Testes instrumentados/UI para callback aprovado, cancelado, erro, emulador ausente, app recriado durante `PROCESSING` e reenvio do mesmo `purchaseId` sem segunda chamada.
- Em AVD Android 10 com o emulador instalado, preencher manualmente `CIELO_CLIENT_ID` e `CIELO_ACCESS_TOKEN` em `cielo.local.properties`; executar uma transação de teste e selecionar Sucesso, Cancelado e Erro no emulador.
- Antes de concluir, executar build e testes definidos pela task. Faça uma varredura para confirmar que os valores das credenciais não aparecem no Git, logs ou artefatos.

### Restrições

- Não use cartão real, não publique na Cielo Store e não faça chamada de produção.
- Não registre dados de cartão, máscara, código de autorização, token ou payload Base64.
- Não faça commit de `cielo.local.properties`.
- Se o contrato da Cielo divergir da documentação atual, pare e reporte a divergência antes de substituir o comportamento local validado.

## Referências

- https://developercielo.github.io/manual/cielo-lio
- https://docs.cielo.com.br/split/docs/app-proprio-cielo-smart

# T5 — Integração local com Emulador Cielo via Deep Link

## Status: Concluída ✅

Esta tarefa integrou o SellCieApplication com o emulador da Cielo LIO utilizando o padrão de Deep Link. A implementação garante o disparo da Intent para a maquininha e o processamento seguro das respostas (sucesso, erro e cancelamento) via callback.

## Prompt de implementação (Atualizado)

Você é um especialista em Android Kotlin, Engenharia de Software e Integrações Cielo. Refatore a integração do SellCie com o Emulador Cielo seguindo os padrões recomendados:

1. **Padronização e Constantes**:
   - Elimine "Magic Numbers" criando um `CieloConstants.kt` para códigos de retorno do Sandbox (ex: 51 para Saldo Insuficiente) e chaves de metadados JSON.
   - Centralize todas as strings (mensagens de erro, labels de UI) no `res/values/strings.xml` para suportar i18n.

2. **Arquitetura e Desacoplamento**:
   - Refatore o `CieloPaymentErrorHandler` para retornar IDs de recursos de string (`@StringRes`) em vez de texto bruto.
   - Utilize o padrão `UiText` no `TicketViewModel` para enviar mensagens para a UI de forma segura e desacoplada.
   - Ajuste o `CieloPaymentContract` para realizar o parsing flexível do JSON da Cielo, identificando automaticamente se o retorno é uma `Order` completa ou um erro simplificado.

3. **UX e Resiliência**:
   - Adicione suporte a scroll na tela de comprovante para garantir que todos os detalhes da transação e botões de ação estejam acessíveis.
   - Capture e exiba metadados ricos da transação (Bandeira, NSU, Autorização, Máscara do Cartão) no recibo final.

4. **Testes e Validação**:
   - Atualize a suíte de testes unitários para validar o mapeamento de todos os "Magic Values" da Cielo.
   - Garanta que os testes de integração verifiquem a persistência correta dos metadados no SQLite.

## Decisões Técnicas

- **Gson**: Adotado para garantir a integridade do JSON enviado e recebido, eliminando erros de concatenação de strings.
- **SQLite v3**: O banco de dados local foi migrado para a versão 3 para incluir a coluna `metadata` na tabela `purchase_attempt`.
- **Deep Link**: O app agora dispara `lio://payment` e escuta o callback em `sellcie://payment-result`.
- **Modo Emulador**: Na ausência de credenciais reais no `cielo.local.properties`, o gateway assume credenciais dummy para permitir o teste imediato com o app sample da Cielo.

## Cobertura de Testes

- **Unitários**: Validam o contrato de dados Cielo, o handler de erros (Magic Values) e a lógica do ViewModel.
- **Integração**: Validam o ciclo de vida da persistência no banco de dados e a renderização dinâmica do comprovante com dados da Cielo.

## Referências

- Documentação Cielo LIO Deep Link: [link](https://developercielo.github.io/manual/cielo-lio)
- Padrões de Projeto Android: MVVM + Clean Architecture.

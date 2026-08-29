# 🎫 SellCie — A Revolução na Venda de Ingressos Presencial

Bem-vindo ao **SellCie**, a solução definitiva para venda de ingressos diretamente em terminais inteligentes. Este MVP (Produto Mínimo Viável) foi desenhado para oferecer uma experiência de compra fluida, segura e totalmente integrada ao ecossistema **Cielo Smart**.

## 🚀 O que é o SellCie?
O SellCie é um aplicativo Android focado em produtores de eventos que precisam de mobilidade e agilidade. Ele transforma a maquininha de cartão em um ponto de venda completo: do catálogo de eventos à entrega do ingresso com QR Code.

### Por que usar o SellCie?
- **Agilidade no Checkout**: Seleção rápida de ingressos e cálculo automático de totais.
- **Segurança Total**: Integração nativa com a Cielo LIO, garantindo transações criptografadas.
- **Funcionamento Offline**: O catálogo e o histórico de compras são persistidos localmente, garantindo que você nunca perca uma venda.
- **Experiência Digital**: Ingressos gerados na hora com identificadores únicos para validação futura.

---

## 🛠 Funcionalidades do MVP

### 1. Catálogo Inteligente
Visualize seus eventos em uma interface moderna e intuitiva. Controle a quantidade de ingressos com um simples toque e tenha o resumo do pedido sempre à mão.

### 2. Pagamento de Última Geração
Integração via **Deep Link** com a Cielo. Ao confirmar a compra, o SellCie aciona automaticamente o módulo de pagamento da maquininha, suportando:
- ✅ Cartão de Débito e Crédito.
- ✅ Simulação de cenários (Saldo insuficiente, cancelamento, etc) via emulador.
- ✅ Retorno automático com dados de NSU e Autorização.

### 3. Comprovante e Gestão
Recibos detalhados que exibem não apenas os itens comprados, mas também os dados técnicos da transação (Bandeira, NSU), garantindo transparência para o vendedor e o cliente.

### 4. Meus Ingressos
Após a aprovação, o cliente tem acesso imediato aos ingressos individuais, cada um com seu próprio QR Code (identificador único) pronto para ser validado na entrada do evento.

---

## 📖 Como Usar (Guia Rápido)

1.  **Explorar**: Abra o app e veja a lista de eventos disponíveis.
2.  **Selecionar**: Escolha a quantidade de ingressos para cada evento.
3.  **Confirmar**: Clique em "Continuar" para revisar seu pedido.
4.  **Pagar**: Clique em "Confirmar Compra". O app abrirá a interface da Cielo. Passe o cartão e digite a senha.
5.  **Receber**: Após o sucesso, veja seu comprovante detalhado.
6.  **Acessar**: Clique em "Ver meus ingressos" para visualizar os QR Codes da sua compra.

---

## 🔧 Informações para Desenvolvedores

### Tecnologias
- **UI**: Jetpack Compose (Material 3)
- **Arquitetura**: MVVM + Clean Architecture
- **Persistência**: SQLite (Local storage)
- **Integração**: Deep Link Cielo Smart + Gson para payloads JSON.

### Execução de Teste (Emulador)
Para testar a integração com a maquininha:
1. Certifique-se de ter o **Cielo LIO Emulator** instalado.
2. Utilize o comando: `./gradlew assembleCieloEmulator`
3. Execute a variante de build `cieloEmulator`.

---
**SellCie**: Vendendo experiências, simplificando pagamentos.

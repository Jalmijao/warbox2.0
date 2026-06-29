# Compra de Assinatura pelo Usuário

## Contexto
Usuários podem adquirir planos de assinatura para ter acesso ao catálogo de vídeos.
Um usuário pode possuir apenas uma assinatura ativa por vez.

---

## Cenário: Compra bem-sucedida de assinatura

**Given** que o usuário está autenticado
**And** que não possui uma assinatura ativa
**And** que escolheu um plano disponível (ex: Premium - R$29,90/mês)
**When** completa o pagamento com sucesso
**Then** o sistema cria a assinatura com status "ativo"
**And** define uma data de expiração (30 dias a partir de hoje)
**And** dispara o evento `SubscriptionActivatedEvent`
**And** o usuário recebe confirmação por email

---

## Cenário: Usuário tenta comprar segunda assinatura

**Given** que o usuário já possui uma assinatura ativa
**When** tenta adquirir um novo plano
**Then** recebe erro 409 Conflict
**And** mensagem: "Você já possui uma assinatura ativa"
**And** pode optar por fazer upgrade/downgrade do plano atual

---

## Cenário: Cancelamento de pagamento

**Given** que o usuário iniciou o processo de compra
**When** cancela o pagamento antes de confirmar
**Then** nenhuma assinatura é criada
**And** a tentativa é registrada para auditoria

---

## Cenário: Pagamento falha

**Given** que o usuário completou o formulário de pagamento
**When** o pagamento é processado e falha (cartão recusado, etc)
**Then** recebe erro 402 Payment Required
**And** a assinatura NÃO é ativada
**And** o usuário pode tentar novamente

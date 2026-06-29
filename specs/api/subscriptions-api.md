# Subscriptions API

## GET /api/v1/plans
- Purpose: listar planos disponíveis
- Auth: public
- Response 200: list of plans with `id`, `nome`, `preco_reais`, `duracao_dias`, `ativo`

## POST /api/v1/subscriptions
- Purpose: comprar novo plano / iniciar pagamento
- Auth: Bearer
- Request:
  - `planId`: uuid
  - `paymentMethod`: { type: "card" | "boleto", details: { ... } } (or tokenized payment reference)
- Response 201:
```json
{ "data": { "subscriptionId": "sub-001", "status": "pending", "paymentUrl": "https://..." }, "error": null }
```
- After payment success, system publishes `SubscriptionActivatedEvent`.

## GET /api/v1/subscriptions/{userId}
- Purpose: obter assinatura atual do usuário
- Auth: Bearer (user or admin)
- Response 200: subscription details

## PUT /api/v1/subscriptions/{id}/cancel
- Purpose: cancelar assinatura
- Auth: Bearer (owner or admin)
- Request: `{ "reason": "user_request" }`
- Response 200: updated subscription (status = canceled)

## POST /api/v1/subscriptions/{id}/renew
- Purpose: renovar assinatura manualmente (quando aplicável)
- Auth: Bearer

## Notes
- Integrar com gateway de pagamento (Stripe/PayPal/PagSeguro) via `payment-service`.
- Use webhooks do gateway para criar `PaymentProcessedEvent`.
- Verifique idempotência usando `paymentId`.

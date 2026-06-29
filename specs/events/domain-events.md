# Especificação de Eventos de Domínio

Este documento descreve os eventos trocados entre contextos no Warbox 2.0. Cada evento inclui: nome, propósito, produtor, consumidores, versão, payload (exemplo), garantias de entrega, idempotência, política de retry/DLQ, campos de correlação e observabilidade.

---

## Convenções gerais
- Versionamento: use `v1`, `v2`, ... no campo `version` do payload e no nome do schema.
- Formato de serialização recomendado: JSON (para prototipagem) e Avro/Protobuf para produção (compactação + esquema explicitamente versionado).
- Campos comuns em todos os eventos:
  - `eventName`: string
  - `version`: string
  - `occurredAt`: ISO8601 UTC
  - `correlationId`: string (opcional, para rastreabilidade entre serviços)
  - `traceId`: string (opcional, para tracing distribuído)

---

# Lista de eventos principais
- `VideoUploadedEvent` (v1)
- `VideoProcessingStartedEvent` (v1)
- `VideoProcessingCompletedEvent` (v1)
- `VideoProcessingFailedEvent` (v1)
- `SubscriptionActivatedEvent` (v1)
- `SubscriptionExpiredEvent` (v1)
- `SubscriptionCanceledEvent` (v1)
- `PaymentProcessedEvent` (v1)
- `VideoAccessGrantedEvent` (v1)
- `VideoAccessDeniedEvent` (v1)
- `UsuarioCriadoEvent` (v1)

---

## Template para cada evento
- **Producer:** serviço/endpoint que emite o evento
- **Consumers:** lista de serviços que devem reagir
- **Payload (exemplo):** JSON exemplar
- **Schema (sumário):** campos obrigatórios e tipos
- **Delivery guarantee:** at-least-once / at-most-once
- **Idempotency:** como consumir de forma idempotente
- **Retry/DLQ policy:** tentativa e quando enviar para DLQ
- **Retention / persistence:** onde e por quanto tempo armazenar
- **Metrics / observability:** métricas sugeridas

---

## VideoUploadedEvent (v1)
- Producer: `upload-service` (API `POST /videos/upload`)
- Consumers: `video-processor`, `catalog-service`, `audit-service`
- Purpose: sinalizar que um novo arquivo foi recebido e está pronto para processamento assíncrono.

Payload (exemplo):
```json
{
  "eventName": "VideoUploadedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T12:00:00Z",
  "correlationId": "req-123",
  "videoId": "uuid-video-001",
  "uploaderId": "uuid-admin-01",
  "originalUrl": "s3://warbox/videos/original/uuid-video-001.mp4",
  "title": "Como Aprender Python",
  "category": "Programação",
  "tags": ["python", "tutorial"],
  "sizeBytes": 104857600,
  "durationSeconds": 3600
}
```

Schema (resumo):
- `videoId` (UUID) - required
- `uploaderId` (UUID) - required
- `originalUrl` (string) - required
- `title` (string) - required
- `occurredAt` (datetime) - required

Delivery guarantee: at-least-once.
Idempotency: consumers devem deduplicar por `videoId` + `eventName` + `version` (persistir last-seen event id ou timestamp).
Retry/DLQ: retries exponenciais até N=5; se continuar falhando, enviar para DLQ com metadata do erro.
Retention: reter raw-event por 7 dias no broker; arquivar em event-store por 90 dias (opcional).
Metrics: `video_uploaded.count`, `video_uploaded.latency` (do upload ao publish).

---

## VideoProcessingStartedEvent (v1)
- Producer: `video-processor` (worker que inicia o job)
- Consumers: `catalog-service`, `audit-service`
- Purpose: indica que o processamento das resoluções foi iniciado.

Payload (exemplo):
```json
{
  "eventName": "VideoProcessingStartedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T12:05:00Z",
  "correlationId": "req-123",
  "videoId": "uuid-video-001",
  "processingJobId": "job-789"
}
```

Delivery guarantee: at-least-once.
Idempotency: ignore eventos repetidos por `processingJobId`.
Retry/DLQ: curto retry (3 tentativas) antes de DLQ.
Metrics: `video_processing.started.count`.

---

## VideoProcessingCompletedEvent (v1)
- Producer: `video-processor`
- Consumers: `catalog-service`, `access-control`, `notification-service`, `analytics`
- Purpose: notificar que as resoluções foram geradas e o vídeo está disponível.

Payload (exemplo):
```json
{
  "eventName": "VideoProcessingCompletedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T12:30:00Z",
  "correlationId": "req-123",
  "videoId": "uuid-video-001",
  "resolutions": ["480p", "720p", "1080p"],
  "urls": {
    "480p": "s3://warbox/videos/480p/uuid-video-001.m3u8",
    "720p": "s3://warbox/videos/720p/uuid-video-001.m3u8",
    "1080p": "s3://warbox/videos/1080p/uuid-video-001.m3u8"
  },
  "thumbnailUrl": "s3://warbox/thumbnails/uuid-video-001.jpg",
  "processingTimeSeconds": 1500
}
```

Schema (resumo):
- `videoId` required
- `resolutions` array of strings required
- `urls` map required (keys correspond to resolutions)

Delivery guarantee: at-least-once.
Idempotency: deduplicar por `videoId` + `version`.
Retry/DLQ: retries exponenciais; DLQ se inválido o payload.
Metrics: `video_processing.completed.count`, `video_processing.duration`.

---

## VideoProcessingFailedEvent (v1)
- Producer: `video-processor`
- Consumers: `audit-service`, `notification-service`, `catalog-service`
- Purpose: indicar falha no processamento para tomada de ação/manual retry.

Payload (exemplo):
```json
{
  "eventName": "VideoProcessingFailedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T12:20:00Z",
  "correlationId": "req-123",
  "videoId": "uuid-video-001",
  "errorCode": "FFMPEG_FAIL",
  "errorMessage": "codec not supported",
  "attempts": 2
}
```

Policy: notificar ops/owner, oferecer interface para retry manual.

---

## SubscriptionActivatedEvent (v1)
- Producer: `subscription-service` (após confirmação de pagamento)
- Consumers: `access-control`, `notification-service`, `analytics`, `recommendation-service`
- Purpose: notificar que uma assinatura foi ativada e o usuário passa a ter acesso.

Payload (exemplo):
```json
{
  "eventName": "SubscriptionActivatedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T13:00:00Z",
  "correlationId": "req-456",
  "subscriptionId": "sub-001",
  "userId": "uuid-user-123",
  "planId": "plan-premium",
  "startAt": "2026-06-28T00:00:00Z",
  "expiresAt": "2026-07-28T23:59:59Z",
  "paymentId": "pay-999"
}
```

Delivery guarantee: at-least-once. Idempotency: consumers deduplicam por `subscriptionId`.
Retry/DLQ: retries; DLQ se payload inválido.
Metrics: `subscription.activated.count`.

---

## SubscriptionExpiredEvent (v1)
- Producer: `subscription-service` (job scheduler que processa expirações)
- Consumers: `access-control`, `notification-service`, `analytics`
- Purpose: indicar que a assinatura expirou e acesso deve ser revogado.

Payload (exemplo):
```json
{
  "eventName": "SubscriptionExpiredEvent",
  "version": "v1",
  "occurredAt": "2026-07-29T00:00:00Z",
  "correlationId": "cron-expire-2026-07-29",
  "subscriptionId": "sub-001",
  "userId": "uuid-user-123",
  "expiresAt": "2026-07-28T23:59:59Z"
}
```

Idempotency: deduplicar por `subscriptionId` + `occuredAt`.

---

## SubscriptionCanceledEvent (v1)
- Producer: `subscription-service` (usuário ou admin cancela)
- Consumers: `access-control`, `notification-service`, `billing`, `analytics`

Payload (exemplo):
```json
{
  "eventName": "SubscriptionCanceledEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T14:00:00Z",
  "correlationId": "req-789",
  "subscriptionId": "sub-002",
  "userId": "uuid-user-456",
  "reason": "user_request"
}
```

---

## PaymentProcessedEvent (v1)
- Producer: `payment-gateway-integration` (apos resposta do gateway)
- Consumers: `subscription-service`, `billing`, `notification-service`, `audit-service`
- Purpose: informar resultado do pagamento (sucesso/falha)

Payload (exemplo):
```json
{
  "eventName": "PaymentProcessedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T13:05:00Z",
  "correlationId": "req-456",
  "paymentId": "pay-999",
  "status": "success",
  "amount": 29.90,
  "currency": "BRL",
  "method": "card",
  "userId": "uuid-user-123",
  "metadata": { "gateway": "stripe" }
}
```

Garantia: at-least-once; idempotência por `paymentId`.

---

## VideoAccessGrantedEvent / VideoAccessDeniedEvent (v1)
- Producer: `access-control`
- Consumers: `analytics`, `notification-service`, `recommendation-service`
- Purpose: registrar decisões de autorização para playback e gerar métricas.

Payload exemplo (granted):
```json
{
  "eventName": "VideoAccessGrantedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T13:10:00Z",
  "correlationId": "req-999",
  "userId": "uuid-user-123",
  "videoId": "uuid-video-001",
  "reason": "subscription_active"
}
```

Payload exemplo (denied):
```json
{
  "eventName": "VideoAccessDeniedEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T13:10:01Z",
  "correlationId": "req-999",
  "userId": "uuid-user-000",
  "videoId": "uuid-video-001",
  "reason": "subscription_expired",
  "denialCode": "SUB_EXPIRED"
}
```

---

## UsuarioCriadoEvent (v1)
- Producer: `identity-service` (após registro bem-sucedido)
- Consumers: `notification-service`, `subscription-service`, `analytics`

Payload exemplo:
```json
{
  "eventName": "UsuarioCriadoEvent",
  "version": "v1",
  "occurredAt": "2026-06-28T09:00:00Z",
  "correlationId": "req-111",
  "userId": "uuid-user-987",
  "email": "maria@example.com",
  "displayName": "Maria Souza"
}
```

Observação: nunca incluir `senha_hash` no evento.

---

# Regras operacionais e recomendações
- Publicar schemas: mantenha JSON Schema/Avro/Protobuf no repositório `specs/events/schemas/` e gere validações automáticas na CI.
- Contract tests: adote consumer-driven contract tests (Pact ou schema validation) para garantir compatibilidade.
- Tracing e correlação: preencha `correlationId` em todas as requisições de entrada e propague como header para eventos.
- Segurança: campos sensíveis (ex: dados de cartão) não devem ser enviados em eventos; usar tokens ou referências seguras.
- Idempotência: consumidores devem guardar um registro de `eventId`/`correlationId` ou usar version + aggregateId para assegurar processamento idempotente.
- Retry/DLQ: padronizar política de reentrega e centralizar monitoramento de DLQ para operações manuais.
- Observability: instrumentar métricas de contagem, erros e latências por evento e criar alertas para taxas de erro elevadas.

---

# Próximos passos práticos
1. Gerar arquivos de schema para cada evento em `specs/events/schemas/` (JSON Schema ou Avro).
2. Implementar validação de schema nos produtores (librearia de serialização).
3. Adicionar testes de contrato na CI para consumidores críticos (`catalog-service`, `access-control`, `subscription-service`).

---

Arquivo gerado automaticamente por Spec Driven Development. Edite conforme necessidades do time.

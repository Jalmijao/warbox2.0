# Contextos Limitados (Bounded Contexts)

## 🎯 Visão Geral

O sistema Warbox 2.0 é dividido em múltiplos contextos (microserviços lógicos) que se comunicam através de eventos.

---

## 1. Bounded Context: Gestão de Identidade e Acesso

**Propósito**: Autenticação, autorização e gerenciamento de usuários.

### Entidades Principais
- `Usuário`
- `Admin`

### Responsabilidades
- Registrar novos usuários
- Autenticar usuários (login/logout)
- Gerenciar tokens (JWT)
- Validar permissões

### Invariantes
- Email deve ser único
- Apenas um email por usuário
- Senha debe ser segura
- Apenas admins pode gerenciar outros usuários

### Eventos Produzidos
- `UsuarioCriadoEvent`
- `UsuarioAutenticadoEvent`
- `UsuarioDesativadoEvent`

### Eventos Consumidos
- (nenhum importante para este contexto inicialmente)

### APIs Externas
```
POST /auth/register
POST /auth/login
POST /auth/logout
GET /users/{id}
PUT /users/{id}
DELETE /users/{id} (soft delete)
```

---

## 2. Bounded Context: Upload e Processamento de Vídeos

**Propósito**: Gerenciar upload, validação e processamento de vídeos.

### Entidades Principais
- `Vídeo`

### Responsabilidades
- Validar upload de vídeos
- Armazenar arquivo original
- Processar vídeo (gerar resoluções)
- Gerenciar metadados do vídeo
- Registrar erros de processamento

### Invariantes
- Apenas admins podem fazer upload
- Arquivo deve ser MP4
- Máximo 10GB por arquivo
- Vídeo deve ser processado para 480p, 720p e 1080p

### Eventos Produzidos
- `VideoUploadedEvent`
- `VideoProcessingStartedEvent`
- `VideoProcessingCompletedEvent`
- `VideoProcessingFailedEvent`
- `VideoRemovedEvent`

### Eventos Consumidos
- (nenhum obrigatório)

### Serviços Externos Usados
- Object Storage (S3, Cloudinary, etc)
- Message Queue (para fila de processamento)
- Video Processing Service (ffmpeg, MediaConvert, etc)

### APIs Externas
```
POST /videos/upload
GET /videos/{id}
PUT /videos/{id}
DELETE /videos/{id}
GET /videos/{id}/status
```

---

## 3. Bounded Context: Catálogo de Vídeos

**Propósito**: Busca, descoberta e exploração de vídeos.

### Entidades Principais
- `Vídeo (somente leitura)`

### Responsabilidades
- Indexar vídeos para busca
- Oferecer filtros e facetas
- Ordenar resultados
- Gerar recomendações

### Invariantes
- Apenas vídeos com status "disponivel" aparecem
- Resultados devem ser paginados

### Eventos Produzidos
- (nenhum - somente consome)

### Eventos Consumidos
- `VideoUploadedEvent`
- `VideoProcessingCompletedEvent`
- `VideoRemovedEvent`

### Banco de Dados
- Elasticsearch ou similiar (para busca full-text)
- Cache Redis (para resultados populares)

### APIs Externas
```
GET /videos/search?q=...&category=...&sort=...
GET /videos/trending
GET /videos/recommended
GET /categories
```

---

## 4. Bounded Context: Gerenciamento de Assinaturas

**Propósito**: Planos, assinaturas e ciclo de vida de pagamento.

### Entidades Principais
- `Plano`
- `Assinatura`

### Responsabilidades
- Criar e gerenciar planos
- Processar compra de assinatura
- Renovar assinaturas
- Cancelar assinaturas
- Expirar assinaturas automaticamente
- Gerar recibos

### Invariantes
- Um usuário = máximo uma assinatura ativa
- Assinatura só é ativa se pagamento confirmado
- Expiração é automática
- Não pode cancelar já cancelada

### Eventos Produzidos
- `SubscriptionActivatedEvent`
- `SubscriptionExpiredEvent`
- `SubscriptionCanceledEvent`
- `SubscriptionSuspendedEvent`
- `SubscriptionRenewedEvent`

### Eventos Consumidos
- `UsuarioCriadoEvent` (para registrar contato)
- `PagamentoProcessadoEvent` (payment gateway)

### Integrações Externas
- Payment Gateway (Stripe, PagSeguro, etc)
- Email Service (para confirmações)
- Job Scheduler (para expiração automática)

### APIs Externas
```
POST /subscriptions
GET /subscriptions/{user_id}
GET /subscriptions/{id}
PUT /subscriptions/{id}/cancel
POST /subscriptions/{id}/renew
GET /plans
POST /plans
PUT /plans/{id}
DELETE /plans/{id}
```

---

## 5. Bounded Context: Controle de Acesso

**Propósito**: Autorização para acessar vídeos (verificação de assinatura).

### Entidades Principais
- (valores imutáveis)

### Responsabilidades
- Validar se usuário pode assistir vídeo
- Verificar assinatura ativa
- Implementar cache de permissões
- Registrar acessos para auditoria

### Invariantes
- Acesso só permitido se:
  - Usuário autenticado ✓
  - Assinatura ativa ✓
  - Não expirada ✓
  - Vídeo disponível ✓

### Eventos Produzidos
- `VideoAccessGrantedEvent`
- `VideoAccessDeniedEvent`

### Eventos Consumidos
- `SubscriptionActivatedEvent`
- `SubscriptionExpiredEvent`
- `VideoProcessingCompletedEvent`

### Cache
- Redis: permissões de acesso (TTL: 5 minutos)

### APIs Externas
```
POST /access/verify
GET /access/check?user_id=...&video_id=...
```

---

## 6. Bounded Context: Reprodução de Vídeo

**Propósito**: Streaming de vídeo adaptativo baseado em qualidade.

### Entidades Principais
- `Sessão de Reprodução`

### Responsabilidades
- Servir vídeos em múltiplas resoluções
- Implementar adaptive bitrate (ABR)
- Registrar visualizações
- Salvar posição de reprodução (timestamp)

### Invariantes
- Apenas servir se acesso autorizado
- Servir apenas resoluções processadas

### Eventos Produzidos
- `VideoPlaybackStartedEvent`
- `VideoPlaybackCompletedEvent`
- `VideoPlaybackPausedEvent`
- `VisualizacaoRegistradaEvent`

### Eventos Consumidos
- (nenhum obrigatório)

### Banco de Dados
- Timestamp de reprodução (posição)
- Histórico de visualizações

### APIs Externas
```
GET /videos/{id}/stream?resolution=720p
GET /videos/{id}/manifest.m3u8 (HLS)
POST /videos/{id}/playback/start
POST /videos/{id}/playback/pause
POST /videos/{id}/playback/resume
GET /videos/{id}/playback/position
```

---

## 7. Bounded Context: Notificações e Comunicação

**Propósito**: Enviar notificações e emails para usuários.

### Responsabilidades
- Enviar emails de confirmação
- Enviar alertas de assinatura
- Enviar recomendações

### Eventos Consumidos
- `UsuarioCriadoEvent`
- `SubscriptionActivatedEvent`
- `SubscriptionExpiredEvent` (lembrete antes de expirar)
- `VideosRecomendadosEvent`

### Integrações
- Email Service (SendGrid, AWS SES, etc)
- SMS Service (opcional)
- Push Notifications (opcional)

---

## 📡 Mapa de Eventos Entre Contextos

```
[Identity Context]
  └─→ UsuarioCriadoEvent ──→ [Notification Context]
                           └─→ [Subscription Context]

[Upload Context]
  ├─→ VideoUploadedEvent ──→ [Catalog Context]
  └─→ VideoProcessingCompletedEvent ──→ [Access Control Context]
                                        └─→ [Playback Context]

[Subscription Context]
  ├─→ SubscriptionActivatedEvent ──→ [Access Control Context]
  ├─→ SubscriptionExpiredEvent ──→ [Access Control Context]
  └─→ SubscriptionActivatedEvent ──→ [Notification Context]

[Playback Context]
  └─→ VisualizacaoRegistradaEvent ──→ [Recommendation Engine]
```

---

## 🔌 Padrão de Integração

### Síncrono (REST APIs)
- Verificação de acesso (latência crítica)
- Busca de metadados

### Assíncrono (Message Queue)
- Processamento de vídeo
- Notificações
- Atualização de índices (Elasticsearch)
- Estatísticas e analytics

### Sagas Distribuídas
- Compra de assinatura:
  1. [Subscription Context] cria assinatura (pendente)
  2. [Payment Context] processa pagamento
  3. Se sucesso → dispara SubscriptionActivatedEvent
  4. Caso contrário → dispara SubscriptionCanceledEvent

---

## 📦 Anti-Corrupção (Anti-Corruption Layer)

Quando integrar com sistemas legados ou externos, usar layer de tradução:

```
External Payment Gateway
        ↓
[Anti-Corruption Layer]
  - Traduz modelo externo para modelo interno
  - Valida dados
  - Enriquece com contexto do domínio
        ↓
[Subscription Context Model]
```


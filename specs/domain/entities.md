# Modelagem do Domínio

## 📦 Entidades Principais

---

## 1. Usuário

### Descrição
Representa um usuário registrado na plataforma.

### Atributos
| Atributo | Tipo | Obrigatório | Descrição |
|----------|------|------------|-----------|
| `id` | UUID | ✅ | Identificador único |
| `email` | String | ✅ | Email único (case-insensitive) |
| `nome` | String | ✅ | Nome completo do usuário |
| `senha_hash` | String | ✅ | Hash da senha (nunca armazenar plaintext) |
| `data_criacao` | DateTime | ✅ | Data de registro na plataforma |
| `data_atualizacao` | DateTime | ✅ | Última atualização de perfil |
| `ativo` | Boolean | ✅ | Se o usuário está ativo (padrão: true) |
| `tipo` | Enum | ✅ | "usuario" ou "admin" |

### Invariantes (Regras)
- Email deve ser único
- Email deve ser válido
- Senha deve ter mínimo 8 caracteres
- Não pode deletar usuário, apenas desativar (soft delete)

### Eventos Disparados
- `UsuarioCriadoEvent`
- `UsuarioDesativadoEvent`

---

## 2. Admin (herança de Usuário)

### Descrição
Usuário com permissões administrativas (tipo = "admin").

### Atributos Adicionais
| Atributo | Tipo | Obrigatório | Descrição |
|----------|------|------------|-----------|
| `permissoes` | List<String> | ✅ | Lista de permissões: "gerenciar_videos", "gerenciar_assinaturas", "gerenciar_usuarios" |
| `data_promocao` | DateTime | ✅ | Data em que foi promovido a admin |

### Invariantes
- Apenas admins podem fazer upload de vídeos
- Apenas admins podem criar/editar planos de assinatura
- Deve ter pelo menos um admin ativo no sistema

---

## 3. Vídeo

### Descrição
Representa um vídeo publicado na plataforma.

### Atributos
| Atributo | Tipo | Obrigatório | Descrição |
|----------|------|------------|-----------|
| `id` | UUID | ✅ | Identificador único |
| `titulo` | String | ✅ | Título do vídeo |
| `descricao` | String | ✅ | Descrição detalhada |
| `criador_id` | UUID (Admin) | ✅ | Quem publicou o vídeo |
| `categoria` | String | ✅ | Categoria (ex: "Programação", "Design", "Negócios") |
| `tags` | List<String> | ❌ | Tags para busca |
| `status` | Enum | ✅ | "enviando", "processando", "disponivel", "erro", "removido" |
| `duracao_segundos` | Integer | ❌ | Duração total em segundos |
| `tamanho_bytes` | Long | ❌ | Tamanho do arquivo original |
| `url_arquivo_original` | String | ✅ | URL de armazenamento (S3, etc) |
| `resolucoes_disponiveis` | List<String> | ❌ | Resoluções processadas: "480p", "720p", "1080p" |
| `urls_por_resolucao` | Map<String, String> | ❌ | URL de cada resolução |
| `miniatura_url` | String | ❌ | Thumbnail do vídeo |
| `data_publicacao` | DateTime | ✅ | Quando foi publicado |
| `data_criacao` | DateTime | ✅ | Quando foi uploadado |
| `data_atualizacao` | DateTime | ✅ | Última modificação |
| `visualizacoes` | Integer | ✅ | Contador de views (padrão: 0) |

### Invariantes
- Título não pode estar vazio
- Apenas admins podem publicar
- Uma vez removido, não pode ser recuperado (soft delete)
- Não pode acessar vídeo em status "processando" ou "erro"
- Arquivo original deve estar em formato MP4

### Eventos Disparados
- `VideoUploadedEvent` (quando enviado)
- `VideoProcessingStartedEvent`
- `VideoProcessingCompletedEvent`
- `VideoProcessingFailedEvent`
- `VideoRemovedEvent`

---

## 4. Plano

### Descrição
Representa um plano de assinatura disponível para compra.

### Atributos
| Atributo | Tipo | Obrigatório | Descrição |
|----------|------|------------|-----------|
| `id` | UUID | ✅ | Identificador único |
| `nome` | String | ✅ | Nome do plano (ex: "Básico", "Premium", "Pro") |
| `descricao` | String | ❌ | Descrição do plano |
| `preco_reais` | Decimal | ✅ | Preço em reais (2 casas decimais) |
| `duracao_dias` | Integer | ✅ | Duração da assinatura em dias (ex: 30) |
| `ativo` | Boolean | ✅ | Se o plano está disponível para compra (padrão: true) |
| `limitacoes` | Map<String, Any> | ❌ | Limitações do plano (ex: limite de downloads, qual qualidade máxima) |
| `data_criacao` | DateTime | ✅ | Quando foi criado |
| `data_atualizacao` | DateTime | ✅ | Última atualização |

### Invariantes
- Nome deve ser único
- Preço não pode ser negativo
- Duração deve ser maior que 0
- Não pode deletar plano, apenas desativar

### Eventos Disparados
- `PlanoDescativoEvent`
- `PlanoPrecoChanidoEvent`

---

## 5. Assinatura

### Descrição
Representa uma assinatura ativa de um usuário.

### Atributos
| Atributo | Tipo | Obrigatório | Descrição |
|----------|------|------------|-----------|
| `id` | UUID | ✅ | Identificador único |
| `usuario_id` | UUID | ✅ | Qual usuário é proprietário |
| `plano_id` | UUID | ✅ | Qual plano foi adquirido |
| `status` | Enum | ✅ | "ativa", "cancelada", "expirada", "suspensa" |
| `data_inicio` | DateTime | ✅ | Quando a assinatura começou |
| `data_expiracao` | DateTime | ✅ | Quando a assinatura vai expirar |
| `data_cancelamento` | DateTime | ❌ | Quando foi cancelada (null se ativa) |
| `motivo_cancelamento` | String | ❌ | Razão do cancelamento |
| `id_pagamento` | String | ✅ | ID da transação de pagamento (para auditoria) |
| `data_criacao` | DateTime | ✅ | Quando o registro foi criado |
| `data_atualizacao` | DateTime | ✅ | Última atualização |

### Invariantes
- Um usuário pode ter APENAS uma assinatura ativa por vez
- Uma assinatura ativa não pode ser deletada, apenas cancelada
- Data de expiração deve ser maior que data de início
- Não pode criar assinatura sem pagamento confirmado
- Status "expirada" é automático quando data atual > data_expiracao

### Estados da Assinatura
```
ativa (data_atual < data_expiracao)
    ↓
expirada (data_atual >= data_expiracao) [automático]
    ↓
cancelada (usuário ou admin cancela)
    ↓
suspensa (pagamento recusado, fraude detectada, etc)
```

### Eventos Disparados
- `SubscriptionActivatedEvent`
- `SubscriptionExpiredEvent` (automático, job de expiração)
- `SubscriptionCanceledEvent`
- `SubscriptionSuspendedEvent`
- `SubscriptionRenewedEvent`

---

## 6. Acesso ao Vídeo (Negócio)

### Descrição
Relacionamento entre Usuário e Vídeo. Um usuário pode assistir a um vídeo SE:
1. Está autenticado
2. Tem uma assinatura com status = "ativa"
3. Assinatura não expirou (data_atual < data_expiracao)
4. Vídeo tem status = "disponivel"

### Invariante Principal
**Regra de Acesso**: 
```
pode_assistir = 
  usuario.autenticado AND
  usuario.assinatura.status == "ativa" AND
  usuario.assinatura.data_expiracao > data_atual AND
  video.status == "disponivel"
```

---

## 📊 Relacionamentos

```
Usuário
├── 1:1 → Assinatura (ativa)
└── 1:* → Vídeo (como criador)

Plano
└── 1:* → Assinatura

Assinatura
├── *:1 → Usuário
└── *:1 → Plano

Vídeo
├── *:1 → Usuário (criador/admin)
└── 1:* → Acesso (usuários que podem ver)
```

---

## 🔑 Validações Compartilhadas

| Entidade | Validação |
|----------|-----------|
| Email | Deve ser válido (RFC 5322), único |
| Senha | Min 8 caracteres, uma maiúscula, um número, um caractere especial |
| UUID | Gerado automaticamente (v4) |
| Decimal | Máximo 2 casas decimais para preços |
| DateTime | ISO 8601 com timezone |
| Status | Somente valores do enum permitidos |

---

## 🎯 Próximos Passos

- [ ] Especificar agregados (Aggregate Roots)
- [ ] Especificar Value Objects
- [ ] Definir repositories (persistência)
- [ ] Especificar serviços de domínio

# Agregados e Value Objects

## 📦 Agregados (Aggregate Roots)

Um agregado é um agrupamento de entidades relacionadas tratadas como uma unidade para manutenção da integridade.

---

## 1. Agregado: Usuário com Assinatura

### Aggregate Root: `Usuário`

### Entidades Filhas
- `Assinatura` (zero ou uma)

### Invariantes do Agregado
1. Um usuário pode ter apenas uma assinatura ativa por vez
2. Ao deletar usuário, todas suas assinaturas são canceladas
3. Ao criar assinatura, o usuário deve estar ativo

### Operações principais
- `CriarUsuario(email, nome, senha)`
- `SubscreverPlano(plano_id, id_pagamento)`
- `CancelarAssinatura(motivo)`
- `RenovarAssinatura(plano_id, id_pagamento)`
- `DesativarUsuario()` → cancela assinatura ativa

### Exemplo de serialização
```json
{
  "usuario": {
    "id": "uuid-001",
    "email": "joao@example.com",
    "nome": "João Silva",
    "tipo": "usuario",
    "ativo": true,
    "data_criacao": "2026-01-15T10:30:00Z"
  },
  "assinatura": {
    "id": "uuid-sub-001",
    "plano_id": "uuid-plano-premium",
    "status": "ativa",
    "data_inicio": "2026-06-15T00:00:00Z",
    "data_expiracao": "2026-07-15T23:59:59Z"
  }
}
```

---

## 2. Agregado: Vídeo com Processamento

### Aggregate Root: `Vídeo`

### Value Objects Filhos
- `Resolucao` (480p, 720p, 1080p)
- `ProcessamentoStatus`
- `Metadados`

### Invariantes do Agregado
1. Vídeo em estado "processando" não pode ser acessado
2. Resoluções só são adicionadas após processamento bem-sucedido
3. Vídeo não pode ter status "erro" e "disponivel" simultaneamente
4. Um vídeo removido não pode voltar a ser disponível

### Operações principais
- `UploadarVideo(titulo, descricao, arquivo, admin_id)`
- `IniciarProcessamento()` → muda status para "processando"
- `AdicionarResolucao(resolucao, url)`
- `MarcarComoPronto()` → status = "disponivel"
- `MarcarComoErro(motivo)`
- `RemoverVideo()`
- `IncrementarVisualizacoes()`

### State Machine do Vídeo
```
[enviando] --valida--> [processando] --sucesso--> [disponivel]
                            |
                         --erro--> [erro] (retentar ou remover)

[disponivel] ou [processando] ou [erro] --admin-remove--> [removido]
```

---

## 3. Agregado: Plano de Assinatura

### Aggregate Root: `Plano`

### Value Objects Filhos
- `Preco`
- `Duracao`
- `Limitacoes`

### Invariantes do Agregado
1. Plano ativo não pode ser deletado
2. Mudanças de preço afetam apenas novas assinaturas (retroativo = false)
3. Só pode desativar se não há assinaturas ativas neste plano

### Operações principais
- `CriarPlano(nome, preco, duracao, limitacoes)`
- `AlterarPreco(novo_preco)`
- `AlterarDuracao(nova_duracao)`
- `Desativar()`
- `VerificaAssinaturasAtivas()` → retorna count

---

## 4. Agregado: Assinatura (raiz separada)

### Aggregate Root: `Assinatura`

Pode ser raiz independente se o domínio exigir operações complexas de assinatura.

### Value Objects Filhos
- `PeriodoAssinatura`
- `StatusAssinatura`

### Invariantes
1. Uma assinatura não pode ser modificada após cancelamento
2. Renovação cria nova assinatura (não modifica a atual)
3. Expiração automática é idempotente

### Operações principais
- `Renovar(plano_id, novo_id_pagamento)` → cria nova assinatura
- `Cancelar(motivo)` → muda status
- `Verificar_AindaValida()` → bool
- `TempoRestante()` → timedelta

---

## 💎 Value Objects

Value Objects não têm identidade, representam conceitos medidos por seus atributos.

---

## 1. Resolucao (Video)

```
class Resolucao:
  resolucao: "480p" | "720p" | "1080p"
  url: String
  tamanho_bytes: Long
  criada_em: DateTime
  
  invariantes:
    - resolucao deve ser um dos valores válidos
    - url não pode estar vazia
    - tamanho > 0
    
  métodos:
    == (igualdade comparando os atributos)
    hash()
```

---

## 2. ProcessamentoStatus (Video)

```
class ProcessamentoStatus:
  status: "enviando" | "processando" | "disponivel" | "erro" | "removido"
  percentual_conclusao: 0-100 (null se não começou)
  mensagem_erro: String (null se sem erro)
  data_atualizacao: DateTime
  
  invariantes:
    - se status = "enviando", percentual = null
    - se status = "processando", percentual >= 0
    - se status = "erro", mensagem_erro não é null
    - percentual entre 0 e 100
    
  métodos:
    pode_assistir() -> bool
    esta_finalizando() -> bool
```

---

## 3. Preco (Plano)

```
class Preco:
  valor: Decimal (2 casas decimais)
  moeda: "BRL"
  
  invariantes:
    - valor >= 0
    - moeda deve ser suportada
    
  métodos:
    == (igualdade)
    < > <= >=
    formatar() -> "R$ 29,90"
    em_centavos() -> Long (para banco de dados)
```

---

## 4. Duracao (Plano)

```
class Duracao:
  dias: Integer
  horas: Integer (derivado: dias * 24)
  
  invariantes:
    - dias > 0
    - máximo 365 dias
    
  métodos:
    em_segundos() -> Long
    proxima_expiracao(data_inicio) -> DateTime
```

---

## 5. Email (Usuário)

```
class Email:
  endereco: String
  
  invariantes:
    - deve ser válido (RFC 5322)
    - máximo 254 caracteres
    - único na plataforma
    
  métodos:
    == (case-insensitive)
    hash()
    dominio() -> String (ex: "example.com")
```

---

## 6. PeriodoAssinatura (Assinatura)

```
class PeriodoAssinatura:
  data_inicio: DateTime
  data_expiracao: DateTime
  
  invariantes:
    - data_expiracao > data_inicio
    
  métodos:
    dias_restantes() -> Integer
    percentual_consumido() -> Float (0-100)
    esta_vigente() -> bool
    esta_expirada() -> bool
    duracao_total() -> Integer (dias)
```

---

## 7. StatusAssinatura (Assinatura)

```
class StatusAssinatura:
  valor: "ativa" | "cancelada" | "expirada" | "suspensa"
  data_mudanca: DateTime
  motivo: String (required se cancelada ou suspensa)
  
  invariantes:
    - se cancelada/suspensa, motivo não é null
    
  métodos:
    permite_acesso() -> bool
    pode_renovar() -> bool
```

---

## 📐 Padrões de Implementação

### Comparação de Value Objects
```
Value objects são iguais se todos seus atributos são iguais:

resolucao1 = Resolucao("720p", "s3://url1", 500000000, datetime.now())
resolucao2 = Resolucao("720p", "s3://url1", 500000000, datetime.now())

resolucao1 == resolucao2  # true (mesmo que diferentes instâncias)
```

### Imutabilidade
```
Value objects devem ser imutáveis após criação:

preco = Preco(29.90)
preco.valor = 19.90  # Error! Imutável
```

---

## 🔄 Transações Entre Agregados

Quando um agregado precisa interagir com outro:

```
Cenário: Usuário compra assinatura

1. Agregado Usuário valida assinatura ativa
2. Agregado Plano valida existência e estado
3. Serviço de Domínio: ComprarAssinatura
   - Cria nova assinatura
   - Valida pagamento
   - Dispara evento SubscriptionActivatedEvent
   - Agregado Usuário atualiza reference
   - Persiste ambos

Nota: Apenas agregado Usuário é root, Assinatura é filha.
```

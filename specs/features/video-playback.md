# Reprodução de Vídeo

## Contexto
Apenas usuários com uma assinatura ativa podem assistir aos vídeos.
O sistema deve bloquear acesso quando a assinatura expirar.
O sistema oferece múltiplas resoluções: 480p, 720p e 1080p.

---

## Cenário: Reprodução bem-sucedida com assinatura ativa

**Given** que o usuário está autenticado
**And** que possui uma assinatura ativa e válida
**And** que o vídeo foi processado e está disponível
**When** acessa o vídeo na biblioteca
**Then** o player carrega o vídeo
**And** oferece as resoluções disponíveis (480p, 720p, 1080p)
**And** permite reprodução em tempo real
**And** registra a atividade de visualização para análise

---

## Cenário: Usuário não autenticado tenta assistir

**Given** que o usuário NÃO está autenticado
**When** tenta acessar um vídeo
**Then** é redirecionado para a página de login
**And** recebe erro 401 Unauthorized

---

## Cenário: Assinatura expirada bloqueia acesso

**Given** que o usuário está autenticado
**And** que sua assinatura expirou
**When** tenta reproduzir um vídeo
**Then** recebe erro 403 Forbidden
**And** mensagem: "Sua assinatura expirou. Renove para continuar assistindo"
**And** oferece link para renovar a assinatura

---

## Cenário: Seleção de resolução

**Given** que o vídeo está reproduzindo
**When** o usuário seleciona uma resolução diferente (ex: 1080p → 720p)
**Then** o player muda para a resolução selecionada
**And** o streaming é interrompido minimamente
**And** adapta-se à qualidade de internet disponível

---

## Cenário: Vídeo em processamento não é acessível

**Given** que um vídeo foi uploaded mas ainda está processando
**When** um usuário tenta acessá-lo
**Then** recebe erro 503 Service Unavailable
**And** mensagem: "Vídeo ainda está sendo processado. Tente novamente em breve"

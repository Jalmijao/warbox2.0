# Admin Upload de Vídeo

## Contexto
Apenas administradores podem fazer upload de vídeos. Os vídeos são processados 
para múltiplas resoluções (480p, 720p, 1080p) após o upload.

---

## Cenário: Upload bem-sucedido

**Given** que o usuário é um administrador autenticado
**And** que tenho um arquivo de vídeo válido (MP4, até 10GB)
**When** faço upload do vídeo com título e descrição
**Then** o sistema registra o vídeo com status "processando"
**And** dispara o evento `VideoUploadedEvent`
**And** inicia o processamento assíncrono de resoluções

---

## Cenário: Usuário não-admin não pode fazer upload

**Given** que o usuário NÃO é administrador
**When** tenta fazer upload de um vídeo
**Then** recebe erro 403 Forbidden

---

## Cenário: Validação de arquivo

**Given** que o arquivo tem formato inválido (não é MP4)
**When** tenta fazer upload
**Then** recebe erro 400 Bad Request
**And** mensagem: "Apenas arquivos MP4 são permitidos"
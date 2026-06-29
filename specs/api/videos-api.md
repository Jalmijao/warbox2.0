# Videos API

## Fluxo de Upload em Multipart Chunks

### Etapa 1: POST /api/v1/videos/initiate-upload
- Purpose: iniciar Multipart Upload no storage e obter uploadId
- Auth: Bearer (admin only)
- Request (JSON):
  - `title`: string (required)
  - `description`: string (required)
  - `category`: string (required)
  - `tags`: string[] (optional)
  - `fileSizeBytes`: long (required) - tamanho total estimado
  - `chunkSizeBytes`: long (optional, default 5MB) - tamanho de cada chunk
- Response 200:
```json
{
  "data": {
    "videoId": "uuid-video-001",
    "uploadId": "s3-upload-id-12345",
    "chunkSizeBytes": 5242880,
    "totalChunks": 20,
    "status": "awaiting_chunks",
    "expiresAt": "2026-06-28T14:00:00Z"
  },
  "error": null
}
```
- Side effects: cria Multipart Upload no S3 com `uploadId`

### Etapa 2: GET /api/v1/videos/{id}/chunk-upload-url
- Purpose: obter URL pré-assinada para upload de um chunk específico
- Auth: Bearer (admin)
- Query params:
  - `chunkNumber`: int (1-indexed, ex: 1, 2, 3...)
  - `uploadId`: string (recebido na etapa 1)
- Response 200:
```json
{
  "data": {
    "chunkNumber": 1,
    "uploadUrl": "https://s3.amazonaws.com/warbox/...?partNumber=1&X-Amz-Signature=...",
    "uploadMethod": "PUT",
    "exposedHeaders": ["ETag"],
    "expiresIn": 3600
  },
  "error": null
}
```

### Etapa 3: Upload de chunks direto ao storage
- Cliente faz `PUT` de cada chunk para a `uploadUrl` recebida na etapa 2
- Headers:
  - `Content-Type: video/mp4`
  - `Content-Length: <chunk-size>`
- Storage retorna header `ETag` (use `Expose-Headers` no CORS)
- Cliente pode fazer uploads em paralelo
- Cada chunk pode ter retry independente

### Etapa 4: POST /api/v1/videos/{id}/complete-upload
- Purpose: completar Multipart Upload e iniciar processamento
- Auth: Bearer (admin)
- Request:
```json
{
  "uploadId": "s3-upload-id-12345",
  "chunks": [
    { "chunkNumber": 1, "etag": "\"abc123\"" },
    { "chunkNumber": 2, "etag": "\"def456\"" },
    ...
  ]
}
```
- Response 202 Accepted:
```json
{ "data": { "videoId": "uuid-video-001", "status": "processando" }, "error": null }
```
- Side effects:
  1. Chama `CompleteMultipartUpload` no S3
  2. S3 junta todos os chunks em arquivo único
  3. API publica `VideoUploadedEvent`
  4. Inicia fila de processamento (resoluções 480p, 720p, 1080p)

### Benefícios desta abordagem
- Suporte a arquivos muito grandes (sem limites práticos)
- Uploads paralelos (múltiplos chunks simultâneos)
- Retry granular por chunk (re-upload só do chunk que falhou)
- Banda de uploads distribuída no cliente
- Nunca passa bytes pela API (escala infinita)

## GET /api/v1/videos/{id}
- Purpose: obter metadata de vídeo
- Auth: Bearer (user with active subscription or admin)
- Response 200: full metadata including `resolucoes_disponiveis` when available

## GET /api/v1/videos/{id}/status
- Purpose: checar status de processamento
- Auth: Bearer
- Response 200:
```json
{ "data": { "status": "processando", "percent": 45 }, "error": null }
```

## DELETE /api/v1/videos/{id}
- Purpose: remover vídeo (soft delete)
- Auth: Bearer (admin only)
- Response 204 No Content

## PATCH /api/v1/videos/{id}
- Purpose: atualizar título/descrição/categoria/tags
- Auth: Bearer (admin)
- Response 200: updated metadata

## Notes
- Initiate-upload cria Multipart Upload no S3 e retorna `uploadId` para o cliente coordenar chunks.
- Cada chunk URL é gerada sob demanda via `chunk-upload-url` (cliente não precisa esperar por todas de uma vez).
- Cliente é responsável por gerenciar paralelismo e tentativas de chunk.
- ETags retornado pelo S3 devem ser capturados pelo cliente e enviados em `complete-upload`.
- Video processing só inicia após `complete-upload` bem-sucedido.
- Streaming endpoints (playback) fornecidos pelo `playback-service` com URLs assinadas temporárias.

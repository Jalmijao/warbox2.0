# Playback API

## GET /api/v1/videos/{id}/manifest.m3u8
- Purpose: fornecer manifest HLS (signed URL) para o player
- Auth: Bearer (server validates access via `access-control`)
- Response 200: HLS manifest or redirect to CDN signed URL

## GET /api/v1/videos/{id}/stream
- Purpose: gerar URL temporária assinada para streaming (range requests supported by CDN)
- Auth: Bearer
- Query: `resolution=720p` (optional)
- Response 200: `{ "data": { "streamUrl": "https://cdn/..." }, "error": null }`

## POST /api/v1/videos/{id}/playback/start
- Purpose: registrar início de reprodução
- Auth: Bearer
- Request: `{ "position": 0 }` (seconds)
- Response 202 Accepted
- Side effects: incrementa contador de visualização, publica `VideoPlaybackStartedEvent`

## POST /api/v1/videos/{id}/playback/position
- Purpose: salvar posição de reprodução (resume)
- Auth: Bearer
- Request: `{ "position": 123.5 }`

## Notes
- Use signed short-lived URLs for CDN to avoid proxying bytes through app servers.
- Enforce access checks in `access-control` and cache permissions with Redis for low-latency checks.
- Emit playback events for analytics and recommendations.

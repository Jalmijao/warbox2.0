# API Overview

Este arquivo lista os principais endpoints REST da plataforma Warbox 2.0. Para detalhe por domínio, ver arquivos específicos (`auth-api.md`, `videos-api.md`, `search-api.md`, `subscriptions-api.md`, `playback-api.md`).

## Serviços principais
- Auth/Identity Service: autenticação, registro e gestão de usuários
- Video Service: upload, metadata, gerenciamento e status de vídeo
- Catalog/Search Service: indexação e busca full-text
- Subscription Service: planos e ciclo de vida de assinaturas
- Playback Service: streaming, manifestos e telemetry

## Convenções
- Todas as APIs usam `application/json` para payloads (exceto upload que usa `multipart/form-data`).
- Autenticação: `Authorization: Bearer <token>` (JWT)
- Padrão de resposta: `{ "data": ..., "error": null }` ou `{ "data": null, "error": { "code": ..., "message": ... } }`
- Paginação: `page`, `pageSize` (default `pageSize=20`)
- Time format: ISO 8601 UTC

## Endpoints (resumo)
- Auth: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/users/{id}`
- Videos: `POST /api/v1/videos/upload`, `GET /api/v1/videos/{id}`, `GET /api/v1/videos/{id}/status`, `DELETE /api/v1/videos/{id}`
- Search/Catalog: `GET /api/v1/videos/search`
- Subscriptions: `GET /api/v1/plans`, `POST /api/v1/subscriptions`, `GET /api/v1/subscriptions/{userId}`, `PUT /api/v1/subscriptions/{id}/cancel`
- Playback: `GET /api/v1/videos/{id}/manifest.m3u8`, `GET /api/v1/videos/{id}/stream` (signed URLs), `POST /api/v1/videos/{id}/playback/start`

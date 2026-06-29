# Search / Catalog API

## GET /api/v1/videos/search
- Purpose: busca full-text e filtros por metadados
- Auth: Bearer (user with active subscription)
- Query params:
  - `q`: string (keyword search)
  - `category`: string
  - `creator`: string (creator id or name)
  - `minDuration`, `maxDuration`: integers (seconds)
  - `minResolution`: string ("480p"/"720p"/"1080p")
  - `sort`: `relevance` (default) | `date_desc` | `date_asc` | `duration_desc` | `duration_asc`
  - `page`, `pageSize`
- Response 200:
```json
{
  "data": {
    "total": 150,
    "page": 1,
    "pageSize": 20,
    "results": [
      { "videoId": "uuid-...", "title": "...", "snippet": "...", "publishedAt": "...", "resolutions": ["480p","720p"] }
    ]
  },
  "error": null
}
```

## GET /api/v1/videos/{id}/recommendations
- Purpose: sugestões relacionadas
- Auth: Bearer
- Response: list of `videoId` and score

## Notes
- Service should back results with search engine (Elasticsearch/OpenSearch).
- Support faceted search and aggregations for UI filters.
- Provide autocomplete endpoint: `GET /api/v1/videos/autocomplete?q=prog` returning suggestions.

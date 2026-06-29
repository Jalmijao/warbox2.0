# Auth / Identity API

## POST /api/v1/auth/register
- Purpose: criar novo usuário
- Auth: public
- Request (multipart/json):
  - `email` (string, required)
  - `password` (string, required)
  - `displayName` (string, optional)
- Response 201:
```json
{ "data": { "userId": "uuid-...", "email": "a@b.com", "displayName": "..." }, "error": null }
```
- Errors: 400 Bad Request (invalid input), 409 Conflict (email exists)

## POST /api/v1/auth/login
- Purpose: autenticar usuário
- Auth: public
- Request:
  - `email`, `password`
- Response 200:
```json
{ "data": { "accessToken": "jwt...", "refreshToken": "...", "expiresIn": 3600 }, "error": null }
```
- Errors: 401 Unauthorized

## POST /api/v1/auth/logout
- Purpose: invalidar refresh token
- Auth: Bearer
- Request: none
- Response 204 No Content

## GET /api/v1/users/{id}
- Purpose: obter perfil de usuário
- Auth: Bearer (user or admin)
- Response 200: user profile (omit `senha_hash`)

## Notes
- Passwords are never returned in responses.
- JWT contains `sub=userId`, `roles` and `exp`.
- Refresh tokens stored hashed in DB with expiry.

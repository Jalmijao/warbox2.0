# Backend - Warbox 2.0

## 📋 Pré-requisitos

- Docker >= 24.0
- Docker Compose >= 2.20
- Java 21 (instalado localmente para compilação)
- Maven >= 3.9

## 🚀 Quick Start

### 1. Iniciar MinIO

```bash
cd backend
docker-compose up -d
```

Este comando inicia:
- **MinIO** (S3-compatible storage) - http://localhost:9001

### 2. Setup de buckets

```bash
bash init-infra.sh
```

Este script:
- ✅ Cria buckets no MinIO (`warbox-videos`, `warbox-thumbnails`)

### 3. Compilar e rodar a API

```bash
mvn clean install
mvn spring-boot:run
```

API estará disponível em: **http://localhost:8080**

---

## 📊 Acessar MinIO

| Serviço | URL | Credenciais |
|---------|-----|------------|
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin123 |
| **MinIO API** | http://localhost:9000 | - |

---

## 📁 Estrutura de pastas

```
backend/
├── docker-compose.yml          # MinIO container
├── .env.local                  # Variáveis de ambiente (local dev)
├── init-infra.sh               # Script de setup de buckets
├── pom.xml                     # Maven configuration
├── src/
│   ├── main/java/com/warbox/
│   │   ├── config/             # Configurações (MinIO)
│   │   ├── controller/         # REST endpoints
│   │   ├── service/            # Serviços de negócio
│   │   ├── model/              # Entidades JPA
│   │   ├── repository/         # Data access
│   │   ├── exception/          # Custom exceptions
│   │   ├── storage/            # Storage interfaces e implementações
│   │   └── WarboxApplication.java
│   ├── main/resources/
│   │   ├── application.yml     # Spring config (shared)
│   │   └── application-local.yml  # Local profile
│   └── test/java/              # Testes
└── Dockerfile                  # Image do backend (para deploy)
```

---

## 🔧 Configurações por ambiente

### Local Development
- **Storage**: MinIO (S3-compatible)
- **Profile**: `local`

Arquivo: `application-local.yml`

### AWS Production
- **Storage**: Amazon S3
- **Profile**: `aws`

Arquivo: `application-aws.yml`

Mesmo código, diferentes configurações! 🎯

---

## 🧪 Testar endpoints

### Exemplo: Iniciar upload de vídeo

```bash
curl -X POST http://localhost:8080/api/v1/videos/initiate-upload \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "Tutorial Python",
    "description": "Aprenda Python do zero",
    "category": "Programação",
    "tags": ["python", "tutorial"],
    "fileSizeBytes": 1073741824,
    "chunkSizeBytes": 5242880
  }'
```

---

## 📝 Logs

Ver logs em tempo real:

```bash
# Todos os containers
docker-compose logs -f

# Apenas MinIO
docker-compose logs -f minio
```

---

## 🛑 Parar infraestrutura

```bash
docker-compose down -v
```

(Flag `-v` remove volumes - use com cuidado!)

---

## 🐛 Troubleshooting

### MinIO não conecta
```bash
docker-compose logs minio
# Verificar se porta 9000 está livre
lsof -i :9000
```

---

## 📚 Referências

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [AWS SDK for Java](https://docs.aws.amazon.com/sdk-for-java/)
- [MinIO Documentation](https://docs.min.io/)

---

Criado: 2026-06-28 | Stack: Java 21 | Spring Boot 3.x | Maven | Docker

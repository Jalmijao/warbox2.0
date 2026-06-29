# 🎬 Warbox 2.0

Uma plataforma de vídeos por assinatura onde criadores de conteúdo podem publicar vídeos e disponibilizá-los para assinantes.

O objetivo deste projeto é servir como estudo de arquitetura de software, sistemas distribuídos e desenvolvimento utilizando **Spec Driven Development**.

---

## ✨ Visão Geral

A plataforma permite que administradores publiquem vídeos e gerenciem planos de assinatura, enquanto usuários podem adquirir um plano para consumir o conteúdo disponível.

---

## 🚀 Funcionalidades

### Área Administrativa

* Upload de vídeos
* Gerenciamento de planos de assinatura
* Gerenciamento de usuários
* Publicação e remoção de vídeos

### Área do Usuário

* Cadastro e login
* Compra de assinatura
* Navegação pelo catálogo
* Reprodução de vídeos
* Gerenciamento da própria assinatura

---

## 📋 Regras de Negócio

* Apenas administradores podem publicar vídeos.
* Apenas usuários com uma assinatura ativa podem assistir aos vídeos.
* Um usuário pode possuir apenas uma assinatura ativa por vez.
* Vídeos enviados devem ser processados para múltiplas resoluções (480p, 720p e 1080p).
* O sistema deve impedir acesso a conteúdos protegidos quando a assinatura expirar.

---

## 🎯 Objetivos Técnicos

Este projeto será desenvolvido utilizando Spec Driven Development e terá foco em:

* Arquitetura orientada a eventos
* APIs REST
* Processamento assíncrono
* Streaming de vídeo
* Autenticação e autorização
* Escalabilidade
* Observabilidade
* Testes automatizados

## 🏗️ Arquitetura de Infraestrutura

A infraestrutura é desacoplada através de interfaces, permitindo múltiplas implementações:

### Storage (Video Files)
- **Local Development**: MinIO (S3-compatible) via Docker
- **AWS Production**: Amazon S3
- **Abstração**: `StorageService` interface (implementações: `MinIOStorageService`, `S3StorageService`)

### Banco de Dados
- **Local Development**: PostgreSQL via Docker
- **AWS Production**: Amazon RDS PostgreSQL
- **Abstração**: JPA/Hibernate (banco-agnóstico)

### Message Queue / Event Streaming
- **Local Development**: Kafka via Docker
- **AWS Production**: Managed Streaming for Kafka (MSK)
- **Abstração**: `EventPublisher` interface (implementações: `KafkaEventPublisher`)

### Cache
- **Local Development**: Redis via Docker (opcional)
- **AWS Production**: ElastiCache Redis
- **Abstração**: Spring Cache abstraction

**Benefício**: Deploy do mesmo código em local/staging/production sem mudanças de source.

---

## 🛠️ Stack Técnico

| Componente | Versão | Propósito |
|-----------|--------|----------|
| **Java** | 21 LTS | Runtime, APIs REST, serviços |
| **Spring Boot** | 3.x | Framework web, dependency injection, abstrações |
| **PostgreSQL** | 15+ | Banco de dados relacional |
| **MinIO** | latest | Storage S3-compatible (local) |
| **Kafka** | latest | Message Broker, event streaming |
| **Maven** | 3.9+ | Build tool, dependency management |
| **Docker** | latest | Containerização de infra local |
| **Docker Compose** | - | Orquestração de containers locais |

---

## 📂 Estrutura

```text
specs/      Especificações do sistema
docs/       Documentação complementar
backend/    Código do backend
frontend/   Código do frontend
```

---

## 🗺️ Roadmap

* [x] Definição da visão do produto
* [ ] Especificação das funcionalidades
* [ ] Modelagem do domínio
* [ ] Arquitetura
* [ ] Desenvolvimento do backend
* [ ] Desenvolvimento do frontend
* [ ] Deploy

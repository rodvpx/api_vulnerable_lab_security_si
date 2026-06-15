# Projeto Acadêmico – Ambiente Vulnerável de APIs REST

Este projeto é um laboratório para a disciplina de Segurança da Informação, focado na exploração e mitigação de vulnerabilidades em APIs REST.

O projeto está dividido em dois diretórios independentes para facilitar a apresentação e demonstração:

- `api-vulneravel/`: Contém a API REST com falhas de segurança implementadas intencionalmente (Broken Access Control, Username Enumeration, Missing Validation).
- `api-corrigida/`: Contém a mesma API com as vulnerabilidades corrigidas (JWT, Mensagens Genéricas, Validação de Entrada, Rate Limiting com Bucket4j) e protegida por um API Gateway (Nginx).

## Tecnologias Utilizadas
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Docker & Docker Compose
- Nginx (API Gateway)
- Maven

## Estrutura do Repositório

- `/api-vulneravel`: Ambiente para demonstrar os ataques.
- `/api-corrigida`: Ambiente para demonstrar as mitigações e a arquitetura segura.
- `/docs/demos`: Roteiros de demonstração para a apresentação.

Para rodar qualquer um dos ambientes, entre na pasta desejada e execute:

```bash
docker compose up --build
```

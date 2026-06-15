# Projeto Acadêmico – Laboratório de Segurança em APIs REST

## Tema: 08 – APIs REST

Um laboratório completo para a disciplina de Segurança da Informação focado em demonstrar vulnerabilidades reais em APIs REST, reproduzir ataques e implementar contramedidas de segurança.

---

## 📋 Sumário

1. [Visão Geral](#visão-geral)
2. [Estrutura do Repositório](#estrutura-do-repositório)
3. [Tecnologias Utilizadas](#tecnologias-utilizadas)
4. [Como Executar](#como-executar)
5. [Endpoints da API](#endpoints-da-api)
6. [Vulnerabilidades Implementadas](#vulnerabilidades-implementadas)
   - [1. Broken Access Control](#1-broken-access-control--acesso-indevido)
   - [2. Username Enumeration](#2-username-enumeration--enumeração-de-usuários)
   - [3. Missing Input Validation](#3-missing-input-validation--falta-de-validação)
7. [Correções Implementadas](#correções-implementadas)
   - [1. Autenticação e Autorização com JWT](#1-autenticação-e-autorização-com-jwt)
   - [2. Mensagens Genéricas](#2-mensagens-genéricas-mitigação-de-enumeração)
   - [3. Validação de Entrada](#3-validação-de-entrada-com-constraints)
   - [4. Rate Limiting](#4-rate-limiting-bucket4j--nginx)
8. [Exemplos de Teste (Postman)](#exemplos-de-teste-postman)
9. [Arquitetura](#arquitetura)
10. [Comparação: Vulnerável vs. Corrigida](#comparação-vulnerável-vs-corrigida)
11. [OWASP Top 10 – Mapeamento](#owasp-top-10--mapeamento-das-vulnerabilidades)
12. [Secure SDLC](#relação-com-secure-sdlc)
13. [Observações de Segurança](#observações-de-segurança)
14. [Conclusão](#conclusão)

---

## Visão Geral

Este projeto implementa dois ambientes isolados em Docker:

- **`api-vulneravel/`**: API REST com vulnerabilidades intencionais para fins educacionais
- **`api-corrigida/`**: Mesma API com contramedidas de segurança implementadas

Ambos os ambientes podem ser executados independentemente, permitindo demonstração prática de:
- Como vulnerabilidades são exploradas
- Como contramedidas são implementadas
- Impacto prático das falhas de segurança

---

## Estrutura do Repositório

```
api_vulnerable_lab_security_si/
├── api-vulneravel/          # Ambiente com vulnerabilidades intencionais
│   ├── src/main/java/com/security/lab/
│   │   ├── controller/      # AuthController, UserController, AdminController
│   │   ├── model/           # Entidade User (sem validação)
│   │   ├── repository/      # UserRepository
│   │   └── config/          # DataInitializer
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── pom.xml
├── api-corrigida/           # Ambiente com segurança implementada
│   ├── src/main/java/com/security/lab/
│   │   ├── controller/      # AuthController, UserController, AdminController (protegidos)
│   │   ├── model/           # Entidade User (com validações)
│   │   ├── repository/      # UserRepository
│   │   ├── config/          # DataInitializer (com BCrypt)
│   │   ├── security/        # JwtService, JwtAuthenticationFilter, SecurityConfig
│   │   ├── filter/          # RateLimitFilter, FilterConfig
│   │   ├── exception/       # GlobalExceptionHandler
│   │   └── resources/       # application.properties (com JWT config)
│   ├── nginx.conf           # Configuração do API Gateway
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── pom.xml
└── README.md
```

---

## Tecnologias Utilizadas

- **Backend**: Java 21 + Spring Boot 3
- **Autenticação**: JWT (JSON Web Token) com algoritmo HS256
- **Banco de Dados**: PostgreSQL
- **Containerização**: Docker & Docker Compose
- **API Gateway**: Nginx
- **Segurança**: Spring Security
- **ORM**: Spring Data JPA
- **Rate Limiting**: Bucket4j
- **Hash de Senhas**: BCrypt
- **Build**: Maven

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Postman (para testes)

### 1. Ambiente Vulnerável

```bash
cd api-vulneravel
docker compose up --build
```

**Disponível em:** `http://localhost:8080`

### 2. Ambiente Corrigido

```bash
cd api-corrigida
docker compose up --build
```

**Disponível em:** `http://localhost` (via Nginx) ou `http://localhost:8080` (direto)

---

## Endpoints da API

### API Vulnerável (`api-vulneravel`)

| Método | Endpoint | Autenticação | Status | Descrição |
|--------|----------|--------------|--------|-----------|
| POST | `/auth/register` | ❌ Não | ⚠️ Vulnerable | Registra novo usuário (sem validação) |
| POST | `/auth/login` | ❌ Não | ⚠️ Vulnerable | Login (mensagens distintas) |
| GET | `/users` | ❌ Não | ⚠️ Vulnerable | Lista todos os usuários |
| GET | `/users/{id}` | ❌ Não | ⚠️ Vulnerable | Detalhes de usuário por ID |
| GET | `/admin/dashboard` | ❌ Não | ⚠️ Vulnerable | Painel administrativo |

### API Corrigida (`api-corrigida`)

| Método | Endpoint | Autenticação | Status | Descrição |
|--------|----------|--------------|--------|-----------|
| POST | `/auth/register` | ❌ Não | ✅ Seguro | Registra novo usuário (com validação) |
| POST | `/auth/login` | ❌ Não | ✅ Seguro | Login com JWT + mensagem genérica |
| GET | `/users` | ✅ JWT | ✅ Seguro | Lista usuários (protegido) |
| GET | `/users/{id}` | ✅ JWT | ✅ Seguro | Detalhes de usuário (protegido) |
| GET | `/admin/dashboard` | ✅ JWT+ADMIN | ✅ Seguro | Painel (requer role ADMIN) |

**Dados Iniciais (ambos os ambientes):**
- Email: `admin@empresa.com` / Senha: `admin123` (role: ADMIN)
- Email: `user@empresa.com` / Senha: `user123` (role: USER)

---

## Vulnerabilidades Implementadas

### 1. Broken Access Control — Acesso Indevido

**Descrição:**
Os endpoints `/users`, `/users/{id}` e `/admin/dashboard` não exigem autenticação. Qualquer cliente consegue acessá-los.

**Arquivo:** `api-vulneravel/src/main/java/com/security/lab/controller/UserController.java`

**Código Vulnerável:**

```java
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ⚠️ Vulnerabilidade 1: Acesso indevido (qualquer um acessa a lista)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Accessing all users (VULNERABLE)");
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ⚠️ Vulnerabilidade 1: Acesso indevido (qualquer um acessa detalhes)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Accessing user ID: {} (VULNERABLE)", id);
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

**Impacto:**
- ✗ Exfiltração de dados de usuários
- ✗ Acesso a painel administrativo
- ✗ Reconnaissance para ataques futuros

**Teste no Postman:**

```
Método: GET
URL: http://localhost:8080/users
Headers: (nenhum)
Body: (vazio)

Resposta (200 OK):
[
  {
    "id": 1,
    "name": "Administrador",
    "email": "admin@empresa.com",
    "password": "admin123",
    "role": "ADMIN"
  },
  {
    "id": 2,
    "name": "Usuario Comum",
    "email": "user@empresa.com",
    "password": "user123",
    "role": "USER"
  }
]
```

---

### 2. Username Enumeration — Enumeração de Usuários

**Descrição:**
O endpoint `/auth/login` responde com mensagens diferentes quando o email não existe vs quando a senha está errada, permitindo confirmar contas válidas.

**Arquivo:** `api-vulneravel/src/main/java/com/security/lab/controller/AuthController.java`

**Código Vulnerável:**

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    String email = credentials.get("email");
    String password = credentials.get("password");
    
    logger.info("Login attempt for email: {}", email);

    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
        logger.warn("User not found: {}", email);
        // ⚠️ Vulnerabilidade 2: Mensagem revela que usuário NÃO existe
        return ResponseEntity.status(401).body(Map.of("message", "Usuário não encontrado"));
    }

    User user = userOpt.get();
    if (!user.getPassword().equals(password)) {
        logger.warn("Incorrect password for user: {}", email);
        // ⚠️ Vulnerabilidade 2: Mensagem revela que USUÁRIO EXISTE
        return ResponseEntity.status(401).body(Map.of("message", "Senha incorreta"));
    }

    logger.info("Login successful for: {}", email);
    return ResponseEntity.ok(Map.of("message", "Login realizado com sucesso", "role", user.getRole()));
}
```

**Impacto:**
- ✗ Identificação automática de contas válidas
- ✗ Reduz espaço de busca para brute-force
- ✗ Viabiliza phishing direcionado

**Teste no Postman:**

| Cenário | Método | URL | Body | Resposta |
|---------|--------|-----|------|----------|
| Email não existe | POST | `http://localhost:8080/auth/login` | `{"email":"nao@existe.com","password":"x"}` | `{"message":"Usuário não encontrado"}` |
| Email existe, senha errada | POST | `http://localhost:8080/auth/login` | `{"email":"admin@empresa.com","password":"errada"}` | `{"message":"Senha incorreta"}` |

---

### 3. Missing Input Validation — Falta de Validação

**Descrição:**
O endpoint `/auth/register` aceita qualquer payload sem validar email, comprimento de senha ou campos obrigatórios.

**Arquivo:** `api-vulneravel/src/main/java/com/security/lab/controller/AuthController.java`

**Código Vulnerável:**

```java
// ⚠️ Vulnerabilidade 3: Falta de validação (aceita qualquer coisa)
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody User user) {
    logger.info("Register attempt: {}", user.getEmail());
    // Não há validação de email, senha, etc.
    user.setRole("USER");
    userRepository.save(user);
    return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso!"));
}
```

**Entidade User (vulnerável):**

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;              // ❌ Sem validação
    private String email;             // ❌ Sem @Email
    private String password;          // ❌ Sem @Size
    private String role;
    
    // Getters e Setters...
}
```

**Impacto:**
- ✗ Dados inválidos no banco
- ✗ Erros em cascata
- ✗ Integridade comprometida

**Teste no Postman:**

```
Método: POST
URL: http://localhost:8080/auth/register
Headers: Content-Type: application/json
Body: 
{
  "name": "",
  "email": "invalid-email",
  "password": "123"
}

Resposta (200 OK - ACEITA DADOS INVÁLIDOS):
{
  "message": "Usuário registrado com sucesso!"
}
```

---

## Correções Implementadas

### 1. Autenticação e Autorização com JWT

**Descrição:**
Implementação de autenticação baseada em JWT com criptografia HS256. Cada login gera um token que deve ser enviado no header `Authorization: Bearer <token>`.

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/security/JwtService.java`

**Código — Geração e Validação:**

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ Gera token JWT com email, role, iat e exp
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // ✅ HS256
                .compact();
    }

    // ✅ Valida se token está válido e não expirou
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/security/SecurityConfig.java`

**Código — Autorização por Role:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()              // ✅ Registro/login abertos
                .requestMatchers("/admin/**").hasAuthority("ADMIN")   // ✅ Admin requer ADMIN
                .anyRequest().authenticated()                         // ✅ Demais protegidos
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ✅ BCrypt
    }
}
```

---

### 2. Mensagens Genéricas (Mitigação de Enumeração)

**Descrição:**
Login retorna mensagem genérica para qualquer erro, impossibilitando enumeração de usuários.

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/controller/AuthController.java`

**Código Corrigido:**

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    String email = credentials.get("email");
    String password = credentials.get("password");
    
    logger.info("Secure login attempt for email: {}", email);

    Optional<User> userOpt = userRepository.findByEmail(email);

    // ✅ Correção: Mensagem unificada
    if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
        logger.warn("Invalid login attempt for: {}", email);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Credenciais inválidas"));  // Sempre igual
    }

    User user = userOpt.get();
    String token = jwtService.generateToken(user.getEmail(), user.getRole());

    logger.info("Secure login successful for: {}", email);
    return ResponseEntity.ok(Map.of(
            "message", "Login realizado com sucesso", 
            "token", token  // ✅ Token retornado
    ));
}
```

---

### 3. Validação de Entrada com Constraints

**Descrição:**
Entidade User usa anotações de validação. Controller valida com `@Valid`. Handler trata erros.

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/model/User.java`

**Código Corrigido:**

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "O nome é obrigatório")
    private String name;
    
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String email;
    
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;
    
    private String role;
    
    // Getters e Setters...
}
```

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/controller/AuthController.java`

```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody User user) {  // ✅ @Valid
    logger.info("Secure register attempt: {}", user.getEmail());
    
    if(userRepository.findByEmail(user.getEmail()).isPresent()) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "E-mail já está em uso"));
    }

    user.setRole("USER");
    user.setPassword(passwordEncoder.encode(user.getPassword()));   // ✅ BCrypt
    userRepository.save(user);
    
    return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso!"));
}
```

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);  // ✅ 400
    }
}
```

---

### 4. Rate Limiting (Bucket4j + Nginx)

**Descrição:**
Proteção contra brute-force em duas camadas:
- **Aplicação**: Bucket4j (5 req/min por IP no login)
- **Gateway**: Nginx (10 req/min por IP global)

**Arquivo:** `api-corrigida/src/main/java/com/security/lab/filter/RateLimitFilter.java`

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // ✅ Limite: 5 requisições por minuto
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> createNewBucket());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // ✅ Aplica apenas no /auth/login
        if (request.getRequestURI().startsWith("/auth/login")) {
            String ip = getClientIP(request);
            Bucket bucket = resolveBucket(ip);

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());  // HTTP 429
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**Arquivo:** `api-corrigida/nginx.conf`

```nginx
events {
    worker_connections 1024;
}

http {
    # ✅ Rate Limiting: 10 requisições por minuto por IP
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/m;

    server {
        listen 80;

        location / {
            # ✅ Aplica limite na borda
            limit_req zone=api_limit burst=5 nodelay;
            limit_req_status 429;

            proxy_pass http://api:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

---

## Exemplos de Teste (Postman)

### ✅ Teste 1: Registrar usuário

```
Método: POST
URL: http://localhost:8080/auth/register
Headers: Content-Type: application/json

Body:
{
  "name": "Alice Silva",
  "email": "alice@example.com",
  "password": "senha123"
}

Resposta (200 OK):
{
  "message": "Usuário registrado com sucesso!"
}
```

---

### ✅ Teste 2: Login e obter JWT

```
Método: POST
URL: http://localhost:8080/auth/login
Headers: Content-Type: application/json

Body:
{
  "email": "admin@empresa.com",
  "password": "admin123"
}

Resposta (200 OK):
{
  "message": "Login realizado com sucesso",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbXByZXNhLmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTcxMzk0NTc3MCwiZXhwIjoxNzEzOTQ5MzcwfQ.VT..."
}
```

---

### ✅ Teste 3: Acessar com JWT

```
Método: GET
URL: http://localhost:8080/users
Headers: Authorization: Bearer <JWT_TOKEN>

Body: (vazio)

Resposta (200 OK):
[
  {
    "id": 1,
    "name": "Administrador",
    "email": "admin@empresa.com",
    "password": "$2a$10$...",
    "role": "ADMIN"
  },
  {
    "id": 2,
    "name": "Usuario Comum",
    "email": "user@empresa.com",
    "password": "$2a$10$...",
    "role": "USER"
  }
]
```

---

### ✅ Teste 4: Tentar sem JWT

```
Método: GET
URL: http://localhost:8080/users
Headers: (sem Authorization)

Body: (vazio)

Resposta (401 Unauthorized):
(acesso negado)
```

---

### ✅ Teste 5: Validação rejeitada

```
Método: POST
URL: http://localhost:8080/auth/register
Headers: Content-Type: application/json

Body:
{
  "name": "",
  "email": "invalid",
  "password": "123"
}

Resposta (400 Bad Request):
{
  "password": "A senha deve ter pelo menos 6 caracteres",
  "email": "Formato de e-mail inválido",
  "name": "O nome é obrigatório"
}
```

---

### ✅ Teste 6: Rate Limiting

```
Método: POST
URL: http://localhost:8080/auth/login
Headers: Content-Type: application/json
Body: {"email":"admin@empresa.com","password":"admin123"}

Executar 6 vezes rapidamente.
Na 6ª requisição (429 Too Many Requests):
{
  "message": "Too many requests. Please try again later."
}
```

---

## Arquitetura

### Versão Vulnerável ❌

```
Cliente HTTP
    ↓
Spring Boot API (8080)
    ├─ Sem autenticação
    ├─ Sem validação
    ├─ Sem rate limiting
    ↓
PostgreSQL
```

### Versão Corrigida ✅

```
Cliente HTTP
    ↓
Nginx Gateway (80)
├→ Rate limiting: 10 req/min
├→ Proxy reverso
    ↓
Spring Boot API (8080)
├→ JwtAuthenticationFilter
├→ Rate limiting (Bucket4j): 5 req/min
├→ GlobalExceptionHandler
├→ SecurityConfig (autorização)
    ↓
PostgreSQL
```

---

## Comparação: Vulnerável vs. Corrigida

| Aspecto | Vulnerável ❌ | Corrigida ✅ |
|---------|--------------|----------|
| **Autenticação** | Nenhuma | JWT HS256 |
| **Autorização** | Nenhuma | Por role (ADMIN, USER) |
| **Validação** | Nenhuma | @NotBlank, @Email, @Size |
| **Enumeração** | Mensagens distintas | Mensagens genéricas |
| **Hash de Senha** | Texto plano | BCrypt |
| **Rate Limiting (app)** | Nenhum | Bucket4j: 5 req/min |
| **Rate Limiting (gateway)** | Nenhum | Nginx: 10 req/min |
| **Expiração Token** | N/A | 1 hora |
| **API Gateway** | Nenhum | Nginx |

---

## OWASP Top 10 – Mapeamento das Vulnerabilidades

As vulnerabilidades implementadas mapeiam diretamente para o OWASP Top 10 (2021).

| Vulnerabilidade | Categoria OWASP | Descrição |
|-----------------|-----------------|-----------|
| Endpoints sem autenticação | **A01:2021** – Broken Access Control | Qualquer cliente acessa dados sensíveis |
| Enumeração de usuários | **A07:2021** – Identification and Authentication Failures | Mensagens distintas permitem confirmar contas |
| Falta de validação | **A04:2021** – Insecure Design | Sem constraints no registro |
| Senhas em texto plano | **A02:2021** – Cryptographic Failures | Armazenamento inadequado |
| Sem rate limiting | **A07:2021** – Identification and Authentication Failures | Permite brute-force |

### A01 – Broken Access Control

API vulnerável permite acesso a endpoints sem autenticação:
```
GET /users → Retorna todos os usuários
GET /users/{id} → Retorna detalhes de usuário
GET /admin/dashboard → Acessa painel admin
```

**Mitigação:** JWT + `SecurityConfig` com autorização por role.

---

### A07 – Identification and Authentication Failures

**Enumeração + Sem Rate Limiting:**
```
POST /auth/login com email inexistente → "Usuário não encontrado"
POST /auth/login com senha errada → "Senha incorreta"
```

Atacante faz 1000 tentativas sem proteção.

**Mitigação:** Mensagens genéricas + Bucket4j (5 req/min) + Nginx (10 req/min).

---

### A04 – Insecure Design

Falta de validação no registro:
```
{"name":"","email":"invalid","password":"123"} → Aceito
```

**Mitigação:** Constraints `@NotBlank`, `@Email`, `@Size` + `GlobalExceptionHandler`.

---

### A02 – Cryptographic Failures

Senhas em texto plano:
```java
// ❌ Vulnerável
private String password;  // "admin123" visível
```

**Mitigação:** BCrypt hash irreversível.

---

## Relação com Secure SDLC

Este projeto demonstra o **Secure Software Development Life Cycle (Secure SDLC)**, integrando segurança em todas as fases do desenvolvimento.

### Etapas Representadas

#### 1. **Planejamento** (Fase 1 – Vulnerável)
- Definição de vulnerabilidades educacionais
- Design básico sem segurança
- Escolha de stack (Spring Boot, PostgreSQL, Docker)

#### 2. **Desenvolvimento** (Versão Vulnerável)
- Implementação intencional de falhas
- Endpoints abertos
- Falta de validação
- Senhas em texto plano

#### 3. **Testes e Análise**
- Testes manuais com Postman
- Identificação de vulnerabilidades
- Documentação de achados

#### 4. **Exploração Controlada**
- Demonstração prática de ataques
- Impacto de cada vulnerabilidade
- Ambiente isolado

#### 5. **Correção** (Fase 2 – Corrigida)
- Implementação de JWT
- Validação robusta
- Rate limiting
- API Gateway

#### 6. **Validação Pós-Correção**
- Repetição de testes
- Confirmação de mitigações
- Verificação de regressions

#### 7. **Documentação**
- README completo
- Exemplos Postman
- Arquitetura
- Boas práticas

### Ciclo Visual

```
┌──────────────────────────────────────────────┐
│           Secure SDLC do Projeto             │
├──────────────────────────────────────────────┤
│ 1. Planejamento → Definir vulnerabilidades   │
│ 2. Dev → Implementar com falhas              │
│ 3. Testes → Identificar achados              │
│ 4. Exploração → Demonstrar impacto           │
│ 5. Correção → Aplicar segurança              │
│ 6. Validação → Testar mitigações             │
│ 7. Documentação → Criar referência            │
└──────────────────────────────────────────────┘
```

### Benefícios do Secure SDLC Aplicados

✅ **Redução de Vulnerabilidades em Produção**
- 100% das vulnerabilidades conhecidas foram mitigadas

✅ **Correção Antecipada**
- Custo em dev-time é 100x menor que em produção

✅ **Menor Custo de Manutenção**
- Codebase seguro desde o início

✅ **Maior Conformidade**
- Aderência ao OWASP Top 10
- Padrões de segurança (JWT, BCrypt, Rate Limiting)

✅ **Maior Confiabilidade**
- Endpoints protegidos
- Dados validados
- Proteção contra brute-force
- Senhas seguras

---

## Observações de Segurança

1. **Gerenciamento de Secrets:**
   - JWT_SECRET via variável de ambiente
   - ⚠️ Em produção: Vault, AWS Secrets Manager
   - ⚠️ Nunca commitar secrets

2. **Criptografia de Senhas:**
   - Vulnerável: texto plano
   - Corrigida: BCrypt

3. **Expiração de Token:**
   - Tokens JWT expiram em 1 hora
   - Recomenda-se refresh tokens

4. **Rate Limiting em Produção:**
   - Ajustar conforme tráfego
   - Considerar Redis para distribuição

5. **HTTPS:**
   - Exemplos usam HTTP (lab)
   - Produção: HTTPS obrigatório

---

## Conclusão

Este laboratório implementa com sucesso o ciclo completo de desenvolvimento seguro:

✅ **Fase 1 – Vulnerável:**
- Endpoints sem proteção
- Mensagens que permitem enumeração
- Falta de validação
- Senhas em texto plano

✅ **Fase 2 – Corrigida:**
- Autenticação JWT
- Validação completa
- Mensagens genéricas
- BCrypt + Rate Limiting
- API Gateway (Nginx)

### Aprendizados Práticos

O projeto oferece entendimento completo de:
- 🔓 Vulnerabilidades comuns em APIs REST
- 🔐 Técnicas de exploração controlada
- 🛡️ Implementação de contramedidas
- 🏗️ Arquitetura segura com separação de camadas
- 📋 Aderência a boas práticas (OWASP Top 10, Secure SDLC)

**Pronto para apresentação em Segurança da Informação!** 🎓

---

## Contato e Recursos

- **Tema**: 08 – APIs REST
- **Disciplina**: Segurança da Informação
- **Linguagem**: Java 21 + Spring Boot 3
- **Referência**: OWASP Top 10 (2021)


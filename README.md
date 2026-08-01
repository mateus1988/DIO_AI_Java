# API Inteligente de Gerenciamento de Orçamento com Reconhecimento de Fala

> Projeto desenvolvido no bootcamp **Santander 2026 - AI Java Back-end** da **DIO - Digital Innovation One**
> 
> **Instrutor:** Thiago Poiani

Projeto incrementado com testes unitários.

### 🧪 Testes Implementados

#### TransactionControllerTest
Testes unitários completos para a classe `TransactionController` com cobertura de:

**Testes do Endpoint POST /transactions:**
- ✅ `testCreateTransactionSuccess` - Criar transação com sucesso e validar resposta
- ✅ `testCreateTransactionHttpStatus` - Validar status HTTP 201 CREATED
- ✅ `testCreateTransactionCallsUseCase` - Verificar que o use case é chamado corretamente

**Testes do Endpoint GET /transactions/{category}:**
- ✅ `testListTransactionsByCategorySuccess` - Listar transações por categoria com sucesso
- ✅ `testListTransactionsByCategoryEmpty` - Retornar lista vazia quando não há transações
- ✅ `testListTransactionHttpStatus` - Validar status HTTP 200 OK
- ✅ `testListTransactionsByDifferentCategories` - Listar transações de categorias diferentes

**Testes do Endpoint POST /transactions/ai:**
- ✅ `testTranscribeAudioSuccess` - Transcrever áudio e processar com IA com sucesso
- ✅ `testTranscribeReturnsCorrectContentType` - Validar Content-Type audio/mp3
- ✅ `testTranscribeAudioWithoutFile` - Retornar erro 400 quando arquivo não é informado
- ✅ `testTranscribeCallsTranscriptionModel` - Verificar que o modelo de transcrição é chamado
- ✅ `testTranscribeCallsTextToSpeechModel` - Verificar que o modelo TTS é chamado

**Testes de Integração:**
- ✅ `testMultipleOperations` - Validar múltiplas operações sequenciais

**Tecnologias de Teste:**
- Mock de `ChatClient` com Mockito (simples e sem dependências externas)
- Mock de `TranscriptionModel` e `TextToSpeechModel`
- `MockMvc` para testes de integração HTTP
- Validação de status HTTP, headers e conteúdo JSON

## 📋 Descrição do Projeto

Uma API REST inteligente que permite gerenciar transações financeiras com suporte a reconhecimento de fala (Speech-to-Text), processamento de linguagem natural com IA e síntese de fala (Text-to-Speech) usando OpenAI.

**Funcionalidades principais:**
- 📝 Criar transações financeiras
- 🎤 Transcrever áudio em português para criar transações automaticamente
- 🤖 Processar transações com IA (GPT-4o-mini)
- 🔊 Gerar áudio com resposta da IA
- 🏷️ Listar transações por categoria

---

## 🛠️ Tecnologias Estudadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.2+** - Framework web
- **Spring Web** - Criação de APIs REST
- **Spring Data JPA** - Persistência de dados
- **Spring AI** - Integração com modelos de IA (OpenAI)
  - Transcription (Whisper)
  - Chat Completion (GPT-4o-mini)
  - Text-to-Speech (TTS)

### Banco de Dados
- **MySQL 8** - Banco de dados relacional
- **Hibernate** - ORM (Object-Relational Mapping)

### Integrações
- **OpenAI API** - Processamento de fala e IA
  - Whisper: Reconhecimento de fala
  - GPT-4o-mini: Chat e processamento de linguagem natural
  - TTS: Síntese de fala

### Ferramentas
- **Maven** - Gerenciamento de dependências
- **Lombok** - Redução de boilerplate (getters, setters, construtores)
- **JUnit 5 + AssertJ** - Testes
- **Git** - Controle de versão

---

## 📚 Conhecimentos Adquiridos

### 1. **Arquitetura de API REST**
- Estrutura de controllers, services e repositories
- Padrão de resposta com DTOs (Data Transfer Objects)
- HTTP Status Codes adequados

### 2. **Autenticação e Autorização**
- Integração com Spring Security (conceitos)
- Validação de requests

### 3. **Persistência de Dados**
- Mapeamento de entidades JPA
- Relacionamentos entre entidades
- Conversão de tipos customizados (UUID → String em MySQL)
- Operações CRUD

### 4. **Integração com IA**
- Chamadas a APIs de terceiros (OpenAI)
- Processamento de áudio
- Chat com modelos de linguagem
- Síntese de fala

### 5. **Tratamento de Timeouts e Performance**
- Configuração de timeouts em múltiplas camadas
- Otimização de responses
- Gerenciamento de requisições longas

### 6. **Testes**
- Testes de integração com Spring Boot
- Condicionalidade de testes (EnabledIfEnvironmentVariable)
- Assertions com AssertJ

### 7. **Domain-Driven Design (DDD)**
- Separação entre entidades de domínio e de persistência
- Value Objects (TransactionId)
- Enums para modelar dados (Category)

---

## 🚀 Como Subir a Aplicação

### Pré-requisitos

1. **Java 21+** instalado
2. **Maven 3.8+** instalado
3. **MySQL 8+** rodando
4. **Chave de API da OpenAI** (https://platform.openai.com/api-keys)

### Passo 1: Clonar o Repositório

```bash
git clone <seu-repositorio>
cd budgeting
```

### Passo 2: Configurar Variáveis de Ambiente

Configure sua chave de API da OpenAI:

**Windows:**
```bash
set OPENAI_API_KEY=sua_chave_aqui
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY=sua_chave_aqui
```

### Passo 3: Configurar Banco de Dados

Edite `src/main/resources/application.properties` e configure as credenciais do MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/budgeting
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
```

Crie o banco de dados:
```sql
CREATE DATABASE budgeting CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Passo 4: Instalar Dependências

```bash
mvn clean install
```

### Passo 5: Rodar a Aplicação

```bash
mvn spring-boot:run
```

Ou compile e execute:
```bash
mvn clean package
java -jar target/budgeting-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em: **http://localhost:8080**

---

## 📡 Endpoints da API

### 1. Criar Transação
```http
POST /transactions
Content-Type: application/json

{
  "description": "Compra no mercado",
  "amount": 150.50,
  "category": "ALIMENTACAO"
}
```

**Resposta:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Compra no mercado",
  "amount": 150.50,
  "category": "ALIMENTACAO"
}
```

### 2. Listar Transações por Categoria
```http
GET /transactions/ALIMENTACAO
```

**Resposta:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "description": "Compra no mercado",
    "amount": 150.50,
    "category": "ALIMENTACAO"
  }
]
```

### 3. Transcrever Áudio e Processar com IA
```http
POST /transactions/ai
Content-Type: multipart/form-data

[Enviar arquivo de áudio]
```

**Resposta:** Arquivo MP3 com a síntese de fala da resposta da IA

---

## 🧪 Rodando os Testes

### Testes Unitários
```bash
mvn test
```

### Testes de Integração (requer OPENAI_API_KEY)
```bash
mvn integration-test
# ou
mvn test -Dgroups="integration"
```

---

## 📂 Estrutura do Projeto

```
budgeting/
├── src/
│   ├── main/
│   │   ├── java/dio/budgeting/
│   │   │   ├── domain/              # Entidades de domínio
│   │   │   │   ├── Transaction
│   │   │   │   ├── Category
│   │   │   │   └── TransactionId
│   │   │   ├── application/         # Use Cases
│   │   │   │   ├── PersistTransactionUseCase
│   │   │   │   └── ListTransactionsByCategoryUseCase
│   │   │   └── infrastructure/
│   │   │       ├── http/            # Controllers e DTOs
│   │   │       │   ├── TransactionController
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       └── persistence/     # Repositories e Entities
│   │   │           ├── entity/
│   │   │           ├── repository/
│   │   │           └── converter/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── prompts/
│   └── test/
│       └── java/dio/budgeting/
│           ├── OpenAiChatModelIT.java
│           └── BudgetingApplicationTests.java
├── pom.xml
└── README.md
```

---

## 🔑 Configurações Importantes

### `application.properties`

```properties
# Banco de Dados
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# OpenAI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat-model=gpt-4o-mini

# Transcrição de Áudio (Whisper)
spring.ai.openai.audio.transcription-model=whisper-1
spring.ai.openai.audio.transcription.options.response.language=pt

# Síntese de Fala (TTS)
spring.ai.openai.audio.speech.options.model=gpt-4o-mini-tts
spring.ai.openai.audio.speech.options.voice=nova

# Timeouts
spring.ai.openai.chat.client.timeout=300000
server.tomcat.connection-timeout=300000
server.servlet.session.timeout=30m

# Logs
logging.level.org.springframework.ai=DEBUG
```

---

## 🐛 Problemas Comuns e Soluções

### Erro: "openAiApi cannot be null"
**Solução:** Adicionar `@SpringBootTest` no teste de integração

### Erro: "Incorrect string value for column 'id'"
**Solução:** UUID no MySQL precisa de VARCHAR(36). Usar `@Column(columnDefinition = "VARCHAR(36)")`

### Erro: "Read timed out"
**Solução:** Aumentar timeouts em `application.properties`:
```properties
spring.ai.openai.chat.client.timeout=300000
server.tomcat.connection-timeout=300000
```

---

## 📖 Recursos Adicionais

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring AI Documentation](https://spring.io/projects/spring-ai)
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)
- [DIO - Digital Innovation One](https://www.dio.me/)

---

## 👨‍💻 Autor

Desenvolvido durante o bootcamp **Santander 2026 - AI Java Back-end** com orientação de **Thiago Poiani**

---

## 📝 Licença

Este projeto é fornecido como material educacional do bootcamp.


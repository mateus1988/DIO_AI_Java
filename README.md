# API Inteligente de Gerenciamento de Orçamento com Reconhecimento de Fala

> Projeto desenvolvido no bootcamp ** Itau DIO 2026 - AI Java Back-end** da **DIO - Digital Innovation One**
> 
> **Instrutor:** Thiago Poiani  
> **Aluno / Desenvolvedor:** Mateus

---

## 🚀 Novidades e Melhorias Implementadas

### 🌐 1. Seleção Dinâmica de Idioma na Transcrição (Speech-to-Text)
Agora a API suporta a especificação customizada do **idioma de transcrição** do áudio através do parâmetro opcional `language` (códigos ISO-639-1 como `pt`, `en`, `es`, `fr`, etc.), aproveitando o poder do modelo Whisper da OpenAI através do Spring AI (`OpenAiAudioTranscriptionOptions`).

- **Endpoint `POST /api/transcribe`**:
  - Aceita `file` (MultipartFile) obrigatório.
  - Aceita `language` (String) opcional (ex: `pt`, `en`, `es`).
  - Quando informado, o modelo transcreve com precisão no idioma especificado. Quando omitido, utiliza a detecção/configuração padrão.
- **Endpoint `POST /transactions/ai`**:
  - Aceita `file` (MultipartFile) obrigatório e `language` (String) opcional.
  - Processa a transcrição no idioma escolhido, passa o texto resultante para o assistente de IA registrar transações automaticamente e retorna o áudio sintetizado (TTS).

---

### 🧪 2. Cobertura Abrangente de Testes Unitários

Todos os testes rodam de forma independente e rápida usando **Mockito**, **MockMvc** e banco em memória **H2** para testes de contexto, sem necessidade de dependências externas ativas.

#### 📁 `TranscriptionControllerTest` (Novos Testes)
- ✅ `shouldTranscribeAudioSuccessfullyWithoutLanguage` - Transcrever áudio com sucesso sem especificar idioma (comportamento padrão).
- ✅ `shouldTranscribeAudioSuccessfullyWithLanguage` - Transcrever áudio especificando idioma português (`pt`) via `OpenAiAudioTranscriptionOptions`.
- ✅ `shouldTranscribeAudioSuccessfullyWithEnglishLanguage` - Transcrever áudio especificando idioma inglês (`en`).
- ✅ `shouldFallbackToDefaultWhenLanguageIsBlank` - Fallback para transcrição padrão quando `language` for vazio ou em branco.
- ✅ `shouldReturnBadRequestWhenFileNotProvided` - Validação de erro HTTP 400 Bad Request quando arquivo não é fornecido.

#### 📁 `TransactionControllerTest`
- **POST /transactions:**
  - ✅ `testCreateTransactionSuccess` - Criar transação com sucesso e validar resposta JSON.
  - ✅ `testCreateTransactionHttpStatus` - Validar status HTTP 201 CREATED.
  - ✅ `testCreateTransactionCallsUseCase` - Verificar chamada correta do caso de uso de persistência.
- **GET /transactions/{category}:**
  - ✅ `testListTransactionsByCategorySuccess` - Listar transações filtradas por categoria.
  - ✅ `testListTransactionsByCategoryEmpty` - Retornar lista vazia quando não há transações na categoria.
  - ✅ `testListTransactionHttpStatus` - Validar status HTTP 200 OK.
  - ✅ `testListTransactionsByDifferentCategories` - Listar transações de categorias distintas.
- **POST /transactions/ai:**
  - ✅ `testTranscribeAudioSuccess` - Transcrever áudio e processar com IA e TTS com sucesso.
  - ✅ `testTranscribeAudioWithLanguageSuccess` - Transcrever áudio com opção de idioma informado (`pt`) e processar com IA.
  - ✅ `testTranscribeAudioWithEnglishLanguageSuccess` - Transcrever áudio com idioma em inglês (`en`) e processar com IA.
  - ✅ `testTranscribeReturnsCorrectContentType` - Validar Content-Type `audio/mp3`.
  - ✅ `testTranscribeAudioWithoutFile` - Retornar HTTP 400 Bad Request ao não informar arquivo.
  - ✅ `testTranscribeCallsTranscriptionModel` - Verificar chamada correta do `TranscriptionModel`.
  - ✅ `testTranscribeCallsTextToSpeechModel` - Verificar chamada correta do `TextToSpeechModel`.
- **Testes de Integração entre Endpoints:**
  - ✅ `testMultipleOperations` - Validar múltiplas operações sequenciais de criação e consulta.

---

## 📋 Descrição do Projeto

Uma API REST inteligente que permite gerenciar transações financeiras com suporte a reconhecimento de fala (Speech-to-Text), processamento de linguagem natural com IA e síntese de fala (Text-to-Speech) usando OpenAI.

**Funcionalidades principais:**
- 📝 Criar transações financeiras manualmente ou via áudio.
- 🌐 Transcrever áudio com suporte a escolha de idioma (`pt`, `en`, `es`, etc.).
- 🤖 Processar transações com IA (GPT-4o-mini com Tool Calling).
- 🔊 Gerar áudio MP3 com resposta da IA sintetizada.
- 🏷️ Listar transações por categoria (`GROCERIES`, `AUTO`, `PHARMA`, etc.).

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 4 / 3.x** - Framework backend moderno
- **Spring Web** - Criação de APIs REST
- **Spring Data JPA** - Persistência de dados
- **Spring AI (v2.0.0-M4)** - Integração com OpenAI:
  - **Whisper**: Speech-to-Text com suporte a idiomas
  - **GPT-4o-mini**: Chat Completion & Function Calling (Tools)
  - **TTS**: Síntese de fala

### Banco de Dados
- **MySQL 8** (Produção/Docker)
- **H2 Database** (Testes em memória)
- **Hibernate** - ORM

### Testes & Qualidade
- **JUnit 5**
- **Mockito** & **ArgumentCaptor**
- **AssertJ**
- **MockMvc**
- **Gradle** - Build tool e gerenciamento de dependências

---

## 📡 Endpoints da API

### 1. Transcrever Áudio (Speech-to-Text)
Permite enviar um arquivo de áudio e opcionalmente especificar o idioma desejado.

```http
POST /api/transcribe
Content-Type: multipart/form-data
```

**Parâmetros:**
| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `file` | MultipartFile | Sim | Arquivo de áudio (`.mp3`, `.m4a`, `.wav`, etc.) |
| `language` | String | Não | Código do idioma (ex: `pt`, `en`, `es`, `fr`) |

**Exemplo com cURL (Português):**
```bash
curl -X POST http://localhost:8080/api/transcribe \
  -F "file=@gravacao.m4a" \
  -F "language=pt"
```

**Exemplo com cURL (Inglês):**
```bash
curl -X POST http://localhost:8080/api/transcribe \
  -F "file=@recording.mp3" \
  -F "language=en"
```

---

### 2. Transcrever e Processar Transação com IA
Envia o áudio para transcrição (com idioma opcional), a IA processa o comando, persiste a transação via Tool Calling e retorna a resposta falada em áudio MP3.

```http
POST /transactions/ai
Content-Type: multipart/form-data
```

**Exemplo com cURL:**
```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@gasto.mp3" \
  -F "language=pt" \
  --output resposta.mp3
```

---

### 3. Criar Transação Manualmente
```http
POST /transactions
Content-Type: application/json
```

**Payload:**
```json
{
  "description": "Compra no mercado",
  "amount": 150.50,
  "category": "GROCERIES"
}
```

**Resposta:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Compra no mercado",
  "amount": 150.50,
  "category": "GROCERIES"
}
```

---

### 4. Listar Transações por Categoria
```http
GET /transactions/GROCERIES
```

**Resposta:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "description": "Compra no mercado",
    "amount": 150.50,
    "category": "GROCERIES"
  }
]
```

---

## 🧪 Como Executar os Testes

Para rodar todos os testes unitários do projeto via linha de comando:

**No Windows:**
```bash
.\gradlew.bat test
```

**No Linux / macOS:**
```bash
./gradlew test
```

O relatório visual completo dos testes em HTML pode ser acessado em:
`build/reports/tests/test/index.html`

---

## 🔑 Configuração de Variáveis de Ambiente

Para executar a aplicação com integração real da OpenAI, defina a chave de API:

```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="sua-chave-openai-aqui"

# Windows (CMD)
set OPENAI_API_KEY=sua-chave-openai-aqui

# Linux / macOS
export OPENAI_API_KEY=sua-chave-openai-aqui
```

---

## 👨‍💻 Autor

Projeto desenvolvido e aprimorado como parte da formação **Santander 2026 - AI Java Back-end** na **DIO - Digital Innovation One**.

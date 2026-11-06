package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactonsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TransactionController - Testes Unitários")
class TransactionControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private PersistTransactionUseCase persistTransactionUseCase;
    
    @Mock
    private ListTransactonsByCategoryUseCase listTransactonsByCategoryUseCase;
    
    @Mock
    private TranscriptionModel transcriptionModel;
    
    @Mock
    private TextToSpeechModel textToSpeechModel;
    
    @Mock
    private ChatClient chatClient;
    
    @Mock
    private ChatClient.Builder chatClientBuilder;
    
    @Mock
    private Resource systemPrompt;
    
    private TransactionController controller;
    private ObjectMapper objectMapper;
    
    private UUID transactionId;
    private Transaction transaction;
    
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        // Mock do systemPrompt
        when(systemPrompt.getContentAsString(any())).thenReturn("system prompt");
        
        // Mock do chatClientBuilder
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultTools(any(), any())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        
        controller = new TransactionController(
            persistTransactionUseCase,
            listTransactonsByCategoryUseCase,
            textToSpeechModel,
            transcriptionModel,
            systemPrompt,
            chatClientBuilder
        );
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        
        transactionId = UUID.randomUUID();
        transaction = new Transaction(
            new TransactionId(transactionId),
            "Compra no mercado",
                150,
            Category.GROCERIES
        );
    }

    // ===== TESTES DO ENDPOINT POST /transactions =====

    @Test
    @DisplayName("Deve criar transação com sucesso")
    void testCreateTransactionSuccess() throws Exception {
        // Arrange
        var request = new TransactionRequest("Compra no mercado", Category.GROCERIES, 150);
        var transactionOutput = TransactionOutput.from(transaction);
        
        when(persistTransactionUseCase.execute(any()))
            .thenReturn(transactionOutput);
        
        // Act & Assert
        mockMvc.perform(post("/transactions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("Compra no mercado"))
            .andExpect(jsonPath("$.amount").value(150))
            .andExpect(jsonPath("$.category").value("GROCERIES"));
        
        verify(persistTransactionUseCase, times(1)).execute(any());
    }

    @Test
    @DisplayName("Deve retornar 201 CREATED ao criar transação")
    void testCreateTransactionHttpStatus() throws Exception {
        // Arrange
        var request = new TransactionRequest("Test",  Category.GROCERIES, 100);
        when(persistTransactionUseCase.execute(any())).thenReturn(TransactionOutput.from(transaction));
        
        // Act & Assert
        mockMvc.perform(post("/transactions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve chamar persistTransactionUseCase corretamente")
    void testCreateTransactionCallsUseCase() throws Exception {
        // Arrange
        var request = new TransactionRequest("Compra", Category.GROCERIES, 50);
        when(persistTransactionUseCase.execute(any())).thenReturn(TransactionOutput.from(transaction));
        
        // Act
        mockMvc.perform(post("/transactions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));
        
        // Assert
        verify(persistTransactionUseCase, times(1)).execute(any());
    }

    // ===== TESTES DO ENDPOINT GET /transactions/{category} =====

    @Test
    @DisplayName("Deve listar transações por categoria com sucesso")
    void testListTransactionsByCategorySuccess() throws Exception {
        // Arrange
        var transaction2 = new Transaction(
            new TransactionId(UUID.randomUUID()),
            "Padaria",
            25,
            Category.GROCERIES
        );
        var transactionOutputs = List.of(
            TransactionOutput.from(transaction), 
            TransactionOutput.from(transaction2)
        );
        
        when(listTransactonsByCategoryUseCase.execute(Category.GROCERIES))
            .thenReturn(transactionOutputs);
        
        // Act & Assert
        mockMvc.perform(get("/transactions/GROCERIES")
                .contentType("application/json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].description").value("Compra no mercado"))
            .andExpect(jsonPath("$[1].description").value("Padaria"));
        
        verify(listTransactonsByCategoryUseCase, times(1)).execute(Category.GROCERIES);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há transações")
    void testListTransactionsByCategoryEmpty() throws Exception {
        // Arrange
        when(listTransactonsByCategoryUseCase.execute(Category.GROCERIES))
            .thenReturn(List.of());
        
        // Act & Assert
        mockMvc.perform(get("/transactions/GROCERIES")
                .contentType("application/json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao listar transações")
    void testListTransactionHttpStatus() throws Exception {
        // Arrange
        when(listTransactonsByCategoryUseCase.execute(any()))
            .thenReturn(List.of());
        
        // Act & Assert
        mockMvc.perform(get("/transactions/GROCERIES"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve listar transações de diferentes categorias")
    void testListTransactionsByDifferentCategories() throws Exception {
        // Arrange
        var categories = List.of(
            Category.GROCERIES, 
            Category.AUTO, 
            Category.PHARMA, 
            Category.GROCERIES
        );
        
        for (var category : categories) {
            var tx = new Transaction(
                new TransactionId(UUID.randomUUID()),
                "Transaction " + category,
                100,
                category
            );
            when(listTransactonsByCategoryUseCase.execute(category))
                .thenReturn(List.of(TransactionOutput.from(tx)));
        }
        
        // Act & Assert
        for (var category : categories) {
            mockMvc.perform(get("/transactions/" + category.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value(category.name()));
        }
    }

    // ===== TESTES DO ENDPOINT POST /transactions/ai =====

    @Test
    @DisplayName("Deve transcrever áudio e processar com IA com sucesso")
    void testTranscribeAudioSuccess() throws Exception {
        // Arrange
        byte[] audioContent = "fake audio content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mpeg",
            audioContent
        );
        
        var transcriptionText = "Gastei 150 reais no mercado";
        var aiResponse = "Transação registrada: Compra no mercado - R$ 150,00 - Alimentação";
        var ttsAudio = "fake mp3 audio response".getBytes();
        
        when(transcriptionModel.transcribe(any())).thenReturn(transcriptionText);
        when(textToSpeechModel.call(anyString())).thenReturn(ttsAudio);
        
        // Mock do chatClient chain
        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);
        
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(transcriptionText)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(aiResponse);
        
        // Act & Assert
        mockMvc.perform(multipart("/transactions/ai")
                .file(file))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(header().string("Content-Type", "audio/mp3"));
        
        verify(transcriptionModel, times(1)).transcribe(any());
        verify(textToSpeechModel, times(1)).call(anyString());
    }

    @Test
    @DisplayName("Deve retornar Content-Type audio/mp3")
    void testTranscribeReturnsCorrectContentType() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mpeg",
            "audio content".getBytes()
        );
        
        when(transcriptionModel.transcribe(any())).thenReturn("text");
        when(textToSpeechModel.call(anyString())).thenReturn("audio".getBytes());
        
        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);
        
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("response");
        
        // Act & Assert
        mockMvc.perform(multipart("/transactions/ai").file(file))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "audio/mp3"));
    }

    @Test
    @DisplayName("Deve retornar erro ao não informar arquivo")
    void testTranscribeAudioWithoutFile() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/transactions/ai"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve chamar transcriptionModel corretamente")
    void testTranscribeCallsTranscriptionModel() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mpeg",
            "audio".getBytes()
        );
        
        when(transcriptionModel.transcribe(any())).thenReturn("text");
        when(textToSpeechModel.call(anyString())).thenReturn("audio".getBytes());
        
        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);
        
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("response");
        
        // Act
        mockMvc.perform(multipart("/transactions/ai").file(file));
        
        // Assert
        verify(transcriptionModel, times(1)).transcribe(any());
    }

    @Test
    @DisplayName("Deve chamar textToSpeechModel após processar com IA")
    void testTranscribeCallsTextToSpeechModel() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mpeg",
            "audio".getBytes()
        );
         
        when(transcriptionModel.transcribe(any())).thenReturn("transcribed text");
        when(textToSpeechModel.call(anyString())).thenReturn("audio".getBytes());
         
        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);
         
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("ai response");
        
        // Act
        mockMvc.perform(multipart("/transactions/ai").file(file));
        
        // Assert
        verify(textToSpeechModel, times(1)).call("ai response");
    }

    @Test
    @DisplayName("Deve transcrever áudio com opção de idioma informado e processar com IA")
    void testTranscribeAudioWithLanguageSuccess() throws Exception {
        // Arrange
        byte[] audioContent = "fake audio content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mpeg",
            audioContent
        );
        var transcriptionText = "Gastei 150 reais no mercado";
        var aiResponse = "Transação registrada: Compra no mercado - R$ 150,00 - Alimentação";
        var ttsAudio = "fake mp3 audio response".getBytes();

        when(transcriptionModel.transcribe(any(Resource.class), any(AudioTranscriptionOptions.class)))
            .thenReturn(transcriptionText);
        when(textToSpeechModel.call(anyString())).thenReturn(ttsAudio);

        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(transcriptionText)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(aiResponse);

        // Act & Assert
        mockMvc.perform(multipart("/transactions/ai")
                .file(file)
                .param("language", "pt"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(header().string("Content-Type", "audio/mp3"));

        ArgumentCaptor<OpenAiAudioTranscriptionOptions> optionsCaptor =
            ArgumentCaptor.forClass(OpenAiAudioTranscriptionOptions.class);
        verify(transcriptionModel, times(1)).transcribe(any(Resource.class), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue().getLanguage()).isEqualTo("pt");
    }

    @Test
    @DisplayName("Deve transcrever áudio com idioma em inglês e processar com IA")
    void testTranscribeAudioWithEnglishLanguageSuccess() throws Exception {
        // Arrange
        byte[] audioContent = "fake audio content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.mp3",
            "audio/mpeg",
            audioContent
        );
        var transcriptionText = "I spent 50 dollars at the store";
        var aiResponse = "Transaction registered";
        var ttsAudio = "fake mp3 audio response".getBytes();

        when(transcriptionModel.transcribe(any(Resource.class), any(AudioTranscriptionOptions.class)))
            .thenReturn(transcriptionText);
        when(textToSpeechModel.call(anyString())).thenReturn(ttsAudio);

        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(transcriptionText)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(aiResponse);

        // Act & Assert
        mockMvc.perform(multipart("/transactions/ai")
                .file(file)
                .param("language", "en"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "audio/mp3"));

        ArgumentCaptor<OpenAiAudioTranscriptionOptions> optionsCaptor =
            ArgumentCaptor.forClass(OpenAiAudioTranscriptionOptions.class);
        verify(transcriptionModel, times(1)).transcribe(any(Resource.class), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue().getLanguage()).isEqualTo("en");
    }

    // ===== TESTES DE INTEGRAÇÃO ENTRE ENDPOINTS =====

    @Test
    @DisplayName("Deve suportar múltiplas operações sequenciais")
    void testMultipleOperations() throws Exception {
        // Arrange
        var request = new TransactionRequest("Test", Category.GROCERIES, 100);
        when(persistTransactionUseCase.execute(any())).thenReturn(TransactionOutput.from(transaction));
        when(listTransactonsByCategoryUseCase.execute(Category.GROCERIES))
            .thenReturn(List.of(TransactionOutput.from(transaction)));
        
        // Act - Criar transação
        mockMvc.perform(post("/transactions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
        
        // Listar transações
        mockMvc.perform(get("/transactions/GROCERIES"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
        
        // Assert
        verify(persistTransactionUseCase, times(1)).execute(any());
        verify(listTransactonsByCategoryUseCase, times(1)).execute(Category.GROCERIES);
    }
}


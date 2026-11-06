package dio.budgeting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TranscriptionController - Testes Unitários")
class TranscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TranscriptionModel transcriptionModel;

    private TranscriptionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TranscriptionController(transcriptionModel);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Deve transcrever áudio com sucesso sem especificar idioma (comportamento padrão)")
    void shouldTranscribeAudioSuccessfullyWithoutLanguage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "fake audio bytes".getBytes()
        );

        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("Gastei 50 reais no mercado");

        mockMvc.perform(multipart("/api/transcribe").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Gastei 50 reais no mercado"));

        verify(transcriptionModel, times(1)).transcribe(any(Resource.class));
    }

    @Test
    @DisplayName("Deve transcrever áudio com sucesso especificando o idioma desejado (ex: pt)")
    void shouldTranscribeAudioSuccessfullyWithLanguage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "fake audio bytes".getBytes()
        );

        when(transcriptionModel.transcribe(any(Resource.class), any(AudioTranscriptionOptions.class)))
                .thenReturn("Comprei remédio na farmácia por 80 reais");

        mockMvc.perform(multipart("/api/transcribe")
                        .file(file)
                        .param("language", "pt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Comprei remédio na farmácia por 80 reais"));

        ArgumentCaptor<OpenAiAudioTranscriptionOptions> optionsCaptor =
                ArgumentCaptor.forClass(OpenAiAudioTranscriptionOptions.class);
        verify(transcriptionModel, times(1)).transcribe(any(Resource.class), optionsCaptor.capture());

        assertThat(optionsCaptor.getValue().getLanguage()).isEqualTo("pt");
    }

    @Test
    @DisplayName("Deve transcrever áudio com sucesso para outro idioma (ex: en)")
    void shouldTranscribeAudioSuccessfullyWithEnglishLanguage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "fake audio bytes".getBytes()
        );

        when(transcriptionModel.transcribe(any(Resource.class), any(AudioTranscriptionOptions.class)))
                .thenReturn("I spent 50 dollars at the grocery store");

        mockMvc.perform(multipart("/api/transcribe")
                        .file(file)
                        .param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string("I spent 50 dollars at the grocery store"));

        ArgumentCaptor<OpenAiAudioTranscriptionOptions> optionsCaptor =
                ArgumentCaptor.forClass(OpenAiAudioTranscriptionOptions.class);
        verify(transcriptionModel, times(1)).transcribe(any(Resource.class), optionsCaptor.capture());

        assertThat(optionsCaptor.getValue().getLanguage()).isEqualTo("en");
    }

    @Test
    @DisplayName("Deve utilizar o método padrão quando parâmetro language for vazio ou em branco")
    void shouldFallbackToDefaultWhenLanguageIsBlank() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "fake audio bytes".getBytes()
        );

        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("Texto transcrito padrão");

        mockMvc.perform(multipart("/api/transcribe")
                        .file(file)
                        .param("language", "  "))
                .andExpect(status().isOk())
                .andExpect(content().string("Texto transcrito padrão"));

        verify(transcriptionModel, times(1)).transcribe(any(Resource.class));
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request quando arquivo não for fornecido")
    void shouldReturnBadRequestWhenFileNotProvided() throws Exception {
        mockMvc.perform(multipart("/api/transcribe"))
                .andExpect(status().isBadRequest());
    }
}

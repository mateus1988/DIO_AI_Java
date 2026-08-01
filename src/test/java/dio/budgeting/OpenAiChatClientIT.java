package dio.budgeting;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiChatClientIT {

    @Autowired
    OpenAiChatModel openAiChatModel;

    @Test
    void should_executeSum_when_prompted() {
       var chatClient = ChatClient.builder(openAiChatModel).defaultSystem("Voc é um matemático")
                .build();

      var response = chatClient.prompt("Some 10 mais 20. Depois subtraia 5 do resultado. Exiba apenas o resultado, sem explicações")
              .call().content();


      assertThat(response).contains("25");
      System.out.println(response);

    }
}

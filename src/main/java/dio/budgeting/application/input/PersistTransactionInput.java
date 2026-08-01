package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(@ToolParam(description="Descricao de gasto")String description,
                                      @ToolParam(description="Valor do gasto em centavos")long amount,
                                      @ToolParam(description="Categoria de uma transacao")Category category) {
}

package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionalRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTransactonsByCategoryUseCase {
    private final TransactionalRepository transactionalRepository;

    public ListTransactonsByCategoryUseCase(TransactionalRepository transactionalRepository) {
        this.transactionalRepository = transactionalRepository;
    }

    @Tool(name = "list-transaction-by-category",description = "Lista transacoes financeiras por categoria")
    public List<TransactionOutput> execute(@ToolParam(description="Categoria de uma transacao") Category category) {
        return transactionalRepository.findAllByCategory(category).stream().map(TransactionOutput::from).toList();

    }
}

package dio.budgeting.application;


import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionalRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class PersistTransactionUseCase {

    private final TransactionalRepository transactionalRepository;

    public PersistTransactionUseCase(TransactionalRepository transactionalRepository) {
        this.transactionalRepository = transactionalRepository;
    }

    @Tool(name = "persist-transaction", description = "Persiste uma nova transacao financeira")
    public TransactionOutput execute(PersistTransactionInput  input) {
        var transaction =transactionalRepository.save(
                new Transaction(input.description(), input.amount(), input.category()))
                ;
        return TransactionOutput.from(transaction);
    }
}

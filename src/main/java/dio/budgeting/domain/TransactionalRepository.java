package dio.budgeting.domain;

import java.util.List;
import java.util.UUID;

public interface TransactionalRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAllByCategory(Category category);
}

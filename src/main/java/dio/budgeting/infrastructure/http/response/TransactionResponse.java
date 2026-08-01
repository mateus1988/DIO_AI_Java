package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionOutput;

import java.math.BigDecimal;

public record TransactionResponse(String id, String category, String description, double amount) {

    public static TransactionResponse from(TransactionOutput output) {

        return new TransactionResponse(output.id(), output.category(), output.description(), output.amount());
    }
}

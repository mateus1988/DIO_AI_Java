package dio.budgeting.infrastructure.http.response;

public record TranscribeResponse(
    String filePath,
    String transcription,
    String chatResult
) {}

package com.SwiftSort.MLTicketClassifier.ai;

import com.SwiftSort.MLTicketClassifier.dto.KbSearchResult;
import com.SwiftSort.MLTicketClassifier.model.KnowledgeDocument;
import com.SwiftSort.MLTicketClassifier.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RAGService {

    private final KnowledgeDocumentRepository kbRepo;
    private final EmbeddingService embeddingService;

    public RAGService(KnowledgeDocumentRepository kbRepo, EmbeddingService embeddingService) {
        this.kbRepo = kbRepo;
        this.embeddingService = embeddingService;
    }

    public KbSearchResult findBestMatch(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        List<KnowledgeDocument> docs = kbRepo.findAll();
        if (docs.isEmpty()) {
            return null;
        }
        float[] queryVec = embeddingService.embed(query);
        return docs.stream()
                .map(doc -> {
                    KbSearchResult result = new KbSearchResult();
                    result.setTitle(doc.getTitle());
                    result.setDocumentRef(doc.getDocumentRef());
                    result.setConfidence(embeddingService.cosineSimilarity(queryVec, embeddingService.embed(doc.getContent())));
                    result.setSnippet(extractSnippet(doc.getContent(), query));
                    return result;
                })
                .max(Comparator.comparingDouble(KbSearchResult::getConfidence))
                .filter(r -> r.getConfidence() > 0.1)
                .orElse(null);
    }

    public List<KbSearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        float[] queryVec = embeddingService.embed(query);
        return kbRepo.findAll().stream()
                .map(doc -> {
                    KbSearchResult result = new KbSearchResult();
                    result.setTitle(doc.getTitle());
                    result.setDocumentRef(doc.getDocumentRef());
                    result.setConfidence(embeddingService.cosineSimilarity(queryVec, embeddingService.embed(doc.getContent())));
                    result.setSnippet(extractSnippet(doc.getContent(), query));
                    return result;
                })
                .filter(r -> r.getConfidence() > 0.05)
                .sorted(Comparator.comparingDouble(KbSearchResult::getConfidence).reversed())
                .limit(5)
                .toList();
    }

    private String extractSnippet(String content, String query) {
        if (content == null) {
            return "";
        }
        if (content.length() <= 200) {
            return content;
        }
        int idx = content.toLowerCase(Locale.ROOT).indexOf(query.toLowerCase(Locale.ROOT).split("\\W+")[0]);
        int start = Math.max(0, idx - 60);
        int end = Math.min(content.length(), start + 200);
        return "..." + content.substring(start, end) + "...";
    }
}

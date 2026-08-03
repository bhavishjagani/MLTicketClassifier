package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.ai.RAGService;
import com.SwiftSort.MLTicketClassifier.dto.KbSearchResult;
import com.SwiftSort.MLTicketClassifier.model.KnowledgeDocument;
import com.SwiftSort.MLTicketClassifier.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeDocumentRepository kbRepo;
    private final RAGService ragService;

    public KnowledgeBaseServiceImpl(KnowledgeDocumentRepository kbRepo, RAGService ragService) {
        this.kbRepo = kbRepo;
        this.ragService = ragService;
    }

    @Override
    public List<KbSearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<KbSearchResult> semantic = ragService.search(query);
        if (!semantic.isEmpty()) {
            return semantic;
        }
        return kbRepo.search(query).stream().map(doc -> {
            KbSearchResult r = new KbSearchResult();
            r.setTitle(doc.getTitle());
            r.setDocumentRef(doc.getDocumentRef());
            r.setConfidence(0.5);
            String content = doc.getContent();
            if (content != null && content.length() > 200) {
                int idx = content.toLowerCase().indexOf(query.toLowerCase());
                int start = Math.max(0, idx - 60);
                int end = Math.min(content.length(), start + 200);
                r.setSnippet("..." + content.substring(start, end) + "...");
            } else {
                r.setSnippet(content);
            }
            return r;
        }).toList();
    }

    @Override
    @Transactional
    public KnowledgeDocument uploadDocument(MultipartFile file, String title, String documentRef) throws IOException {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(title);
        doc.setDocumentRef(documentRef);
        doc.setFileType(file.getContentType());
        doc.setContent(new String(file.getBytes(), StandardCharsets.UTF_8));
        return kbRepo.save(doc);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        kbRepo.deleteById(id);
    }

    @Override
    public List<KnowledgeDocument> listAll() {
        return kbRepo.findAll();
    }
}

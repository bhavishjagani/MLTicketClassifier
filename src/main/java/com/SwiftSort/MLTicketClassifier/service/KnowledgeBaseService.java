package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.dto.KbSearchResult;
import com.SwiftSort.MLTicketClassifier.model.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface KnowledgeBaseService {
    List<KbSearchResult> search(String query);
    KnowledgeDocument uploadDocument(MultipartFile file, String title, String documentRef) throws IOException;
    void deleteDocument(Long id);
    List<KnowledgeDocument> listAll();
}

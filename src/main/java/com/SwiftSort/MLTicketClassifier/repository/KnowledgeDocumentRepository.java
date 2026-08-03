package com.SwiftSort.MLTicketClassifier.repository;

import com.SwiftSort.MLTicketClassifier.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    @Query("""
        SELECT d FROM KnowledgeDocument d
        WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(d.content) LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    List<KnowledgeDocument> search(@Param("q") String q);
}
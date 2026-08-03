package com.SwiftSort.MLTicketClassifier;

import com.SwiftSort.MLTicketClassifier.dto.KbSearchResult;
import com.SwiftSort.MLTicketClassifier.model.KnowledgeDocument;
import com.SwiftSort.MLTicketClassifier.service.KnowledgeBaseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    public KnowledgeBaseController(KnowledgeBaseService kbService) {
        this.kbService = kbService;
    }

    @GetMapping("/knowledge-base")
    public String kbPage() {
        return "layout :: layout(view='kb')";
    }

    @GetMapping("/api/kb/search")
    @ResponseBody
    public List<KbSearchResult> search(@RequestParam String q) {
        return kbService.search(q);
    }

    @GetMapping("/api/kb/documents")
    @ResponseBody
    public List<KnowledgeDocument> listDocuments() {
        return kbService.listAll();
    }

    @PostMapping("/api/kb/documents")
    @ResponseBody
    public KnowledgeDocument uploadDocument(@RequestParam("file") MultipartFile file,
                                            @RequestParam("title") String title,
                                            @RequestParam(value = "documentRef", required = false) String documentRef) throws IOException {
        return kbService.uploadDocument(file, title, documentRef);
    }

    @DeleteMapping("/api/kb/documents/{id}")
    @ResponseBody
    public void deleteDocument(@PathVariable Long id) {
        kbService.deleteDocument(id);
    }
}

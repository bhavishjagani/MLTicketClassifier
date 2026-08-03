package com.SwiftSort.MLTicketClassifier.service;

import com.SwiftSort.MLTicketClassifier.model.TicketAttachment;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AttachmentService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "application/pdf",
            "text/plain", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    private final TicketAttachmentRepository attachmentRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public AttachmentService(TicketAttachmentRepository attachmentRepo) {
        this.attachmentRepo = attachmentRepo;
    }

    public TicketAttachment upload(Ticket ticket, MultipartFile file) throws IOException {
        validate(file);
        String safeName = sanitizeFilename(file.getOriginalFilename());
        String storageKey = UUID.randomUUID() + "_" + safeName;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Files.write(dir.resolve(storageKey), file.getBytes());

        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicket(ticket);
        attachment.setFileName(safeName);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setStorageKey(storageKey);
        return attachmentRepo.save(attachment);
    }

    @Transactional(readOnly = true)
    public List<TicketAttachment> getByTicketId(Long ticketId) {
        return attachmentRepo.findByTicket_Id(ticketId);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed");
        }
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

package com.SwiftSort.MLTicketClassifier.config;

import com.SwiftSort.MLTicketClassifier.model.KnowledgeDocument;
import com.SwiftSort.MLTicketClassifier.model.SLA;
import com.SwiftSort.MLTicketClassifier.model.Ticket;
import com.SwiftSort.MLTicketClassifier.model.User;
import com.SwiftSort.MLTicketClassifier.repository.KnowledgeDocumentRepository;
import com.SwiftSort.MLTicketClassifier.repository.SLARepository;
import com.SwiftSort.MLTicketClassifier.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(UserRepository userRepo, PasswordEncoder encoder,
                                  KnowledgeDocumentRepository kbRepo, SLARepository slaRepo) {
        return args -> {
            if (userRepo.findByEmail("admin@swiftsort.dev").isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@swiftsort.dev");
                admin.setPassword(encoder.encode("changeme"));
                admin.setRole(User.Role.ADMIN);
                admin.setActive(true);
                userRepo.save(admin);

                User agent = new User();
                agent.setName("Support Agent");
                agent.setEmail("agent@swiftsort.dev");
                agent.setPassword(encoder.encode("changeme"));
                agent.setRole(User.Role.AGENT);
                agent.setActive(true);
                userRepo.save(agent);
            }
            if (kbRepo.count() == 0) {
                KnowledgeDocument doc1 = new KnowledgeDocument();
                doc1.setTitle("Payment Issues");
                doc1.setContent("If a customer reports payment issues, first verify the transaction ID in the payment gateway. Check if the payment was successful but not reflected in the system. Common causes include: webhook failures, API timeouts, or duplicate transactions. Refunds should be processed within 3-5 business days.");
                doc1.setDocumentRef("KB-001");
                kbRepo.save(doc1);

                KnowledgeDocument doc2 = new KnowledgeDocument();
                doc2.setTitle("Login Problems");
                doc2.setContent("For login issues, verify the user's email format and password complexity requirements. Check if the account is locked due to multiple failed attempts. Reset password functionality sends an email with a temporary link valid for 24 hours.");
                doc2.setDocumentRef("KB-002");
                kbRepo.save(doc2);

                KnowledgeDocument doc3 = new KnowledgeDocument();
                doc3.setTitle("Subscription Management");
                doc3.setContent("Subscription changes take effect at the next billing cycle. Upgrades are prorated, downgrades are not. Cancellations require 30-day notice. Auto-renewal can be disabled in account settings.");
                doc3.setDocumentRef("KB-003");
                kbRepo.save(doc3);
            }
            if (slaRepo.count() == 0) {
                for (Ticket.Priority p : Ticket.Priority.values()) {
                    SLA sla = new SLA();
                    sla.setPriority(p);
                    switch (p) {
                        case CRITICAL -> { sla.setResponseHours(1); sla.setResolutionHours(4); }
                        case HIGH -> { sla.setResponseHours(4); sla.setResolutionHours(12); }
                        case MEDIUM -> { sla.setResponseHours(12); sla.setResolutionHours(24); }
                        case LOW -> { sla.setResponseHours(24); sla.setResolutionHours(48); }
                    }
                    slaRepo.save(sla);
                }
            }
        };
    }
}

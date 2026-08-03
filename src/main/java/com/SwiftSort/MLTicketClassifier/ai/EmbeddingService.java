package com.SwiftSort.MLTicketClassifier.ai;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;

@Service
public class EmbeddingService {

    private static final int DIMENSIONS = 128;

    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[DIMENSIONS];
        }
        float[] vector = new float[DIMENSIONS];
        String[] tokens = text.toLowerCase(Locale.ROOT).split("\\W+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int bucket = Math.floorMod(token.hashCode(), DIMENSIONS);
            vector[bucket] += 1.0f;
        }
        normalize(vector);
        return vector;
    }

    public double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public String toStorageString(float[] vector) {
        if (vector == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.toString();
    }

    public float[] fromStorageString(String stored) {
        if (stored == null || stored.isBlank()) {
            return new float[DIMENSIONS];
        }
        float[] vector = new float[DIMENSIONS];
        String[] parts = stored.split(",");
        for (int i = 0; i < Math.min(parts.length, DIMENSIONS); i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }

    private void normalize(float[] vector) {
        double norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm == 0) {
            return;
        }
        float scale = (float) (1.0 / Math.sqrt(norm));
        for (int i = 0; i < vector.length; i++) {
            vector[i] *= scale;
        }
    }
}

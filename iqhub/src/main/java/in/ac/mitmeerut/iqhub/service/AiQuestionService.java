package in.ac.mitmeerut.iqhub.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import in.ac.mitmeerut.iqhub.entity.Difficulty;
import in.ac.mitmeerut.iqhub.entity.ExamCategory;
import in.ac.mitmeerut.iqhub.entity.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiQuestionService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private static final int MAX_RETRIES = 3;

    public List<Question> generateQuestions(String topic, String subject, ExamCategory category,
                                             Difficulty difficulty, int count) {

        String categoryHint = switch (category) {
            case BANK -> "in the style of Bank PO/Clerk exams (like IBPS, SBI) — quantitative aptitude, reasoning, or banking awareness style";
            case GATE -> "in the style of GATE exam — conceptual, technical, engineering-level depth";
            case PLACEMENT -> "in the style of campus placement/company recruitment tests — aptitude, logical reasoning, and technical basics";
            case PROGRAMMING -> "focused on programming/coding concepts, syntax, logic, and problem-solving";
            case GENERAL -> "general academic/course-level questions";
        };

        String prompt = "Generate exactly " + count + " multiple choice questions on the topic \"" + topic +
                "\" for subject \"" + subject + "\", " + categoryHint + ", at " + difficulty + " difficulty level. " +
                "Respond ONLY with a valid JSON array, no other text, no markdown code fences, no explanation. " +
                "Each item must have exactly these fields: questionText, optionA, optionB, optionC, optionD, correctOption " +
                "(correctOption must be exactly \"A\", \"B\", \"C\", or \"D\").";

        String requestBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":[{\"role\":\"user\",\"content\":" + mapper.valueToTree(prompt) + "}],"
                + "\"temperature\":0.7"
                + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String response = null;
        Exception lastError = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                response = restTemplate.postForObject(API_URL, entity, String.class);
                break;
            } catch (HttpServerErrorException e) {
                lastError = e;
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        if (response == null) {
            throw new RuntimeException("AI service abhi busy hai. Thodi der baad dobara try karo. Detail: "
                    + (lastError != null ? lastError.getMessage() : "unknown"));
        }

        List<Question> questions = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response);

            String aiText = root.get("choices").get(0)
                    .get("message").get("content").asText();

            aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode questionsArray = mapper.readTree(aiText);

            for (JsonNode qNode : questionsArray) {
                Question q = new Question();
                q.setQuestionText(qNode.get("questionText").asText());
                q.setOptionA(qNode.get("optionA").asText());
                q.setOptionB(qNode.get("optionB").asText());
                q.setOptionC(qNode.get("optionC").asText());
                q.setOptionD(qNode.get("optionD").asText());
                q.setCorrectOption(qNode.get("correctOption").asText());
                q.setSubject(subject);
                q.setDifficulty(difficulty);
                q.setExamCategory(category);
                questions.add(q);
            }
        } catch (Exception e) {
            throw new RuntimeException("AI response parse nahi ho paya: " + e.getMessage(), e);
        }

        return questions;
    }
}

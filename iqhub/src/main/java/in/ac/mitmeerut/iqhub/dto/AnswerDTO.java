package in.ac.mitmeerut.iqhub.dto;

public class AnswerDTO {
    private Long questionId;
    private String selectedOption;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
}
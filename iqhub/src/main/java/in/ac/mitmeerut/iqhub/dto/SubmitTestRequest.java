package in.ac.mitmeerut.iqhub.dto;

import java.util.List;

public class SubmitTestRequest {
    private List<AnswerDTO> answers;
    private int tabSwitchCount;
    private boolean autoSubmitted;

    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }
    public int getTabSwitchCount() { return tabSwitchCount; }
    public void setTabSwitchCount(int tabSwitchCount) { this.tabSwitchCount = tabSwitchCount; }
    public boolean isAutoSubmitted() { return autoSubmitted; }
    public void setAutoSubmitted(boolean autoSubmitted) { this.autoSubmitted = autoSubmitted; }
}
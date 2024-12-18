package com.programmers.question;


import com.programmers.answer.AnswerRepository;
import com.programmers.exception.NotFoundDataException;
import com.programmers.page.PageableUtils;
import com.programmers.page.dto.PageRequestDto;
import com.programmers.question.dto.QuestionRegisterRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_SORT_FILED = "id";

    public Question createQuestion(QuestionRegisterRequestDto requestDto) {
        return questionRepository.save(
                Question.builder()
                        .subject(requestDto.subject())
                        .content(requestDto.content())
                        .build()
        );
    }

    public Page<Question> findAllQuestions(PageRequestDto requestDto) {
        Page<Question> questionPage = PageableUtils.getPage(questionRepository, requestDto, DEFAULT_PAGE_SIZE, DEFAULT_SORT_FILED);

        // 각 질문에 대한 답변 수를 계산하여 추가
        questionPage.getContent().forEach(question -> {
            long answerCount = answerRepository.countByQuestion(question); // 답변의 갯수 계산
            question.setAnswerCount(answerCount); // 답변의 갯수를 Question 객체에 설정 (setter를 통해)
        });
        return questionPage;
    }

    public Question findQuestionById(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new NotFoundDataException("Question not found"));
    }
}

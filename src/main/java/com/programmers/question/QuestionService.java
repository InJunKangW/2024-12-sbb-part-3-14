package com.programmers.question;


import com.programmers.answer.AnswerRepository;
import com.programmers.exception.NotFoundDataException;
import com.programmers.page.PageableUtils;
import com.programmers.page.dto.PageRequestDto;
import com.programmers.question.dto.QuestionModifyRequestDto;
import com.programmers.question.dto.QuestionRegisterRequestDto;
import com.programmers.user.SiteUser;
import com.programmers.user.SiteUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.programmers.answer.Answer;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final SiteUserRepository siteUserRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_SORT_FILED = "id";

    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거
                Join<Question, SiteUser> u1 = q.join("siteUser", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("siteUser", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자
            }
        };
    }

    public void siteUserCheck(Long questionId, String username) {
        Question question = findQuestionById(questionId); // 이미 존재하는 메서드
        SiteUser siteUser = question.getSiteUser();

        if (!username.equals(siteUser.getUsername())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403));
        }
    }

    public Question createQuestion(QuestionRegisterRequestDto requestDto, String username) {
        SiteUser siteUser = siteUserRepository.findByUsername(username).orElseThrow(() -> new NotFoundDataException("User not found"));
        return questionRepository.save(
                Question.builder()
                        .siteUser(siteUser)
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

    public Page<Question> getList(PageRequestDto requestDto, String kw) {
        Pageable pageable = PageableUtils.createPageable(requestDto, 10, "createDate");
        Specification<Question> spec = search(kw);
        return this.questionRepository.findAll(spec, pageable);
    }

    public Question findQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new NotFoundDataException("Question not found"));
        long answerCount = answerRepository.countByQuestion(question);
        question.setAnswerCount(answerCount);
        return question;
    }

    public void modifyQuestion(Long questionId, String username, QuestionModifyRequestDto requestDto) {
        Question question = findQuestionById(questionId);
        siteUserCheck(questionId, username);

        question.setSubject(requestDto.subject());
        question.setContent(requestDto.content());
        questionRepository.save(question);
    }

    public void deleteQuestion(Long questionId, String username) {
        siteUserCheck(questionId, username);
        Question question = findQuestionById(questionId);
        questionRepository.delete(question);
    }
}

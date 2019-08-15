package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/")
public class AnswerController {


    @Autowired
    AnswerService answerService;

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired

    QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createAnswer(final AnswerRequest answerRequest, @PathVariable("questionId") final String questionUuId, @RequestHeader final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        if (authorizationService.hasUserSignedIn(authorization)) {
            if (authorizationService.isUserAccessTokenValid(authorization)) {
                UserAuthTokenEntity userAuthTokenEntity = authorizationService.fetchAuthTokenEntity(authorization);
        Question question ;
        try {
            question = questionService.getQuestionForUuId(questionUuId);
        }catch(InvalidQuestionException iQE){
            ErrorResponse errorResponse = new ErrorResponse().message(iQE.getErrorMessage()).code(iQE.getCode()).rootCause(iQE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswer(answerRequest.getAnswer());
        answer.setUuid(UUID.randomUUID().toString());
        answer.setUser(userAuthTokenEntity.getUser());
        ZonedDateTime now = ZonedDateTime.now();
        answer.setDate(now);
        Answer createdAnswer = answerService.createAnswer(answer);

        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
            }
        } else {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> editAnswerContent(AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        if (authorizationService.hasUserSignedIn(authorization)) {
            if (authorizationService.isUserAccessTokenValid(authorization)) {
                UserAuthTokenEntity userAuthTokenEntity = authorizationService.fetchAuthTokenEntity(authorization);
        Answer answer;
        try {
            answer = answerService.isUserAnswerOwner(answerUuId, userAuthTokenEntity, ActionType.EDIT_ANSWER);
        }catch(AuthorizationFailedException authFE){
            ErrorResponse errorResponse = new ErrorResponse().message(authFE.getErrorMessage()).code(authFE.getCode()).rootCause(authFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.FORBIDDEN);
        }catch(AnswerNotFoundException aNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(aNFE.getErrorMessage()).code(aNFE.getCode()).rootCause(aNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }

        answer.setAnswer(answerEditRequest.getContent());
        answer.setDate(ZonedDateTime.now());
        Answer editedAnswer = answerService.editAnswer(answer);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse()
                .id(answerUuId)
                .status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
            }
        } else {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }



    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> deleteAnswer(@PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        if (authorizationService.hasUserSignedIn(authorization)) {
            if (authorizationService.isUserAccessTokenValid(authorization)) {
                UserAuthTokenEntity userAuthTokenEntity = authorizationService.fetchAuthTokenEntity(authorization);
        Answer answer;
        try {
            answer = answerService.isUserAnswerOwner(answerUuId, userAuthTokenEntity, ActionType.DELETE_ANSWER);
        }catch(AuthorizationFailedException authFE){
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner or admin can delete the answer");
        }catch(AnswerNotFoundException aNFE){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }
        answerService.deleteAnswer(answer);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse()
                .id(answerUuId)
                .status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
            }
        } else {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }



    @RequestMapping(method = RequestMethod.GET, path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException, AnswerNotFoundException {
        if (authorizationService.hasUserSignedIn(authorization)) {
            if (authorizationService.isUserAccessTokenValid(authorization)) {
                UserAuthTokenEntity userAuthTokenEntity = authorizationService.fetchAuthTokenEntity(authorization);
        List<Answer> answerList;
        try {
            answerList = answerService.getAnswersForQuestion(questionId);
        }catch(InvalidQuestionException iQE) {
            ErrorResponse errorResponse = new ErrorResponse().message(iQE.getErrorMessage()).code(iQE.getCode()).rootCause(iQE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }catch(AnswerNotFoundException aNFE){
            ErrorResponse errorResponse = new ErrorResponse().message(aNFE.getErrorMessage()).code(aNFE.getCode()).rootCause(aNFE.getMessage());
            return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
        }
        StringBuilder contentBuilder = new StringBuilder();
        getContentsString(answerList, contentBuilder);
        StringBuilder uuIdBuilder = new StringBuilder();
        String questionContentValue = getUuIdStringAndQuestionContent(answerList, uuIdBuilder);
        AnswerDetailsResponse response = new AnswerDetailsResponse()
                .id(uuIdBuilder.toString())
                .answerContent(contentBuilder.toString())
                .questionContent(questionContentValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
            }
        } else {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }

    public static final String getUuIdStringAndQuestionContent(List<Answer> answerList, StringBuilder uuIdBuilder) {
        String questionContent = new String();
        for (Answer answerObject : answerList) {
            uuIdBuilder.append(answerObject.getUuid()).append(",");
            questionContent = answerObject.getQuestion().getContent();
        }
        return questionContent;
    }

    public static final StringBuilder getContentsString(List<Answer> answerList, StringBuilder builder) {
        for (Answer answerObject : answerList) {
            builder.append(answerObject.getAnswer()).append(",");
        }
        return builder;
    }

}

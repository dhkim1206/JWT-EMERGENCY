package com.example.demo.exceptionhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
  반환할 에러의 기본형태
 **/
@Data
@AllArgsConstructor
public class ErrorResult {
    private String code;
    private String message;

    static ErrorResult create(BaseExceptionType baseExceptionType){
        return new ErrorResult(baseExceptionType.getErrorCode(),baseExceptionType.getMessage());
    }
}

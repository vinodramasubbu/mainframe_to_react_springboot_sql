package com.example.survdemo.domain;

public final class InvalidInquiryIdentifierException extends IllegalArgumentException {

    public InvalidInquiryIdentifierException(String message) {
        super(message);
    }
}
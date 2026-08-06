package com.example.survdemo.application;

public interface MonthlyBatchTransaction {

    <T> T execute(TransactionalWork<T> work);

    @FunctionalInterface
    interface TransactionalWork<T> {
        T execute();
    }
}
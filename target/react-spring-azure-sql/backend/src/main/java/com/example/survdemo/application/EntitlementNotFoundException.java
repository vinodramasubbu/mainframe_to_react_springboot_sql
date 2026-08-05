package com.example.survdemo.application;

public final class EntitlementNotFoundException extends RuntimeException {

    public EntitlementNotFoundException() {
        super("Entitlement not found");
    }
}
package com.example.survdemo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SurvdemoApplicationTest {

    @Test
    void enablesNonWebBatchModeOnlyForTheExplicitControlRecordArgument() {
        assertThat(SurvdemoApplication.isBatchLaunch(new String[] {
                "--survdemo.batch.control-record=20260831SRV202608001"
        })).isTrue();
        assertThat(SurvdemoApplication.isBatchLaunch(new String[] {
                "--spring.profiles.active=local"
        })).isFalse();
        assertThat(SurvdemoApplication.isBatchLaunch(new String[0])).isFalse();
    }
}
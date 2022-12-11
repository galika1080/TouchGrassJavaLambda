package com.touchgrass.lambda;

import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class SuggestedTest {

    @Mock
    Context mockContext;

    @Mock
    LambdaLogger mockLogger;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        when(mockContext.getLogger())
            .thenReturn(mockLogger);
    }
    
    @Test
    public void testTest() {
        GetSuggestedEvents gse = new GetSuggestedEvents();

        Map<String, Object> event = Map.of("body", "{ \"email\" : \"agalik2@illinois.edu\" }");
        String result = gse.handleRequest(event, mockContext);

        System.out.println(result);
    }
}
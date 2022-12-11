package com.touchgrass.lambda;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class GetEventsTest {

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
    
    @Disabled
    @Test
    public void testTest() {
        GetEvents ge = new GetEvents();
        String result = ge.handleRequest(null, mockContext);
        
        System.out.println(result);
    }
}
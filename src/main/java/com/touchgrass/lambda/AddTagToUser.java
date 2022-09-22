package com.touchgrass.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Lambda request handler for AddTagToUser API.
 */
public class AddTagToUser implements RequestHandler<Object, Object> {

    @Override
    public Object handleRequest(final Object input, final Context context) {
        // @TODO: Implement handler.
        return input;
    }
}

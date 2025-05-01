package com.triptune.global.security.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
public class HttpRequestEndpointChecker {

    private final DispatcherServlet servlet;

    public boolean isEndpointExist(HttpServletRequest request){
        for(HandlerMapping handlerMapping: servlet.getHandlerMappings()){
            try{
                HandlerExecutionChain executionChain = handlerMapping.getHandler(request);

                if (executionChain != null){
                    return true;
                }
            } catch (Exception e){
                return false;
            }
        }
        return false;
    }
}

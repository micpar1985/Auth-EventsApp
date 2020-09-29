package com.event_app.auth.api;

import com.event_app.auth.domain.Token;
import com.event_app.auth.util.JWTHelper;
import com.event_app.auth.util.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentracing.Span;
import io.opentracing.Tracer;

@RestController
@RequestMapping("/token")
public class TokenAPI {
    //JWTUtil jwtUtil = new JWTMockUtil();
    JWTUtil jwtUtil = new JWTHelper();
    
    @Autowired
    private Tracer tracer;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> getToken(@RequestBody TokenRequestData tokenRequestData) {

    	//---- Start Jaeger Span ----
    	Span span = tracer.buildSpan("Get Token").start();
    	
    	//---- Authenticate User and Create Token ----
        String username = tokenRequestData.getUsername();
        String password = tokenRequestData.getPassword();
        String scopes = tokenRequestData.getScopes();

        ResponseEntity<?> response;
        
        if (username != null && username.length() > 0
                && password != null && password.length() > 0
                && Authenticator.checkPassword(username, password)) {
            Token token = jwtUtil.createToken(scopes);
            response = ResponseEntity.ok(token);
            
            span.setTag("http.status_code", 200);
        }
        else {	// User failed to authenticate
        	response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        	span.setTag("http.status_code", 401);
        }
        
        //---- Finish Jaeger Span and return response ----
        span.finish();
        return response;

    }


}
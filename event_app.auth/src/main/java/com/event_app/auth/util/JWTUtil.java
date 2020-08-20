package com.event_app.auth.util;

import com.event_app.auth.domain.Token;

public interface JWTUtil {
        public boolean verifyToken(String jwt_token);
        public String getScopes(String jwt_token) ;
        public Token createToken(String scopes) ;

}

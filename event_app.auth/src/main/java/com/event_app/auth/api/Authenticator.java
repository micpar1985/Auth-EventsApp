package com.event_app.auth.api;

import com.event_app.auth.domain.Customer;
import com.event_app.auth.domain.Token;
import com.event_app.auth.util.JWTHelper;
import com.event_app.auth.util.JWTUtil;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


public class Authenticator {

    //private static Key key = AuthFilter.key;
    static JWTUtil jwtUtil = new JWTHelper();
    public static Token appUserToken;

    public static boolean checkPassword(String username, String password) {
        // special case for application user
        if (username.equals("ApiClientApp") && password.equals("secret")) {
            return true;
        }
        // make call to customer service
        Customer cust = getCustomerByNameFromCustomerAPI(username);
        // compare name and password
        if (cust != null && cust.getName().equals(username) && cust.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    private static Customer getCustomerByNameFromCustomerAPI(String username) {
        ResponseEntity<Customer> response = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String customersUrl = "http://localhost:8080/api/customers/byName/" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAppUserToken().getToken());
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            response = restTemplate.exchange(customersUrl, HttpMethod.GET, entity, Customer.class);
        } catch (final HttpClientErrorException httpClientErrorException) {
            System.out.println("httpClientErrorException");
        } catch (HttpServerErrorException httpServerErrorException) {
            System.out.println("httpServerErrorException");
        } catch (Exception exception) {
            System.out.println("Other Exception");
        }
        return response.getBody();
    }

    public static Token getAppUserToken() {
        if (appUserToken == null || appUserToken.getToken() == null || appUserToken.getToken().length() == 0) {
            appUserToken = jwtUtil.createToken("ApiClientApp");
        }
        return appUserToken;
    }

}

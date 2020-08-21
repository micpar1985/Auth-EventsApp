package com.event_app.auth.api;

import com.event_app.auth.domain.Customer;
import com.event_app.auth.domain.Token;

import com.event_app.auth.util.JWTHelper;
import com.event_app.auth.util.JWTUtil;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
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
        if(username.equals("ApiClientApp") && password.equals("secret")) {
            return true;
        }
        // make call to customer service
        Customer cust = getCustomerByNameFromCustomerAPI(username);

        // compare name and password
        if(cust != null && cust.getName().equals(username) && cust.getPassword().equals(password)) {
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
            headers.set("Authorization", "Bearer "+getAppUserToken());
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            response = restTemplate.exchange(customersUrl, HttpMethod.GET, entity, Customer.class);
            System.out.println("Result - status ("+ response.getStatusCode() + ") has body: " + response.hasBody());

            if (response.getStatusCode() == HttpStatus.CREATED) {
                try {
                    JSONObject jsonObject = new JSONObject(response.getBody());
                } catch (JSONException e) {
                    throw new RuntimeException("JSONException occurred");
                }
            }
        } catch (final HttpClientErrorException httpClientErrorException) {
            System.out.println("httpClientErrorException");
        } catch (HttpServerErrorException httpServerErrorException) {
            System.out.println("httpServerErrorException");
        } catch (Exception exception) {
            System.out.println("Other Exception");
        }
        return response.getBody();

         /*   try {
            URL url = new URL("http://localhost:8080/api/customers/byname/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            Token token = getAppUserToken();
            conn.setRequestProperty("authorization", "Bearer " + token.getToken());

            if (conn.getResponseCode() != 200) {
                return null;
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output = "";
                String out = "";
                while ((out = br.readLine()) != null) {
                    output += out;
                }
                conn.disconnect();
                return CustomerFactory.getCustomer(output);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;

        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }*/
    }

    public static Token getAppUserToken() {
        if(appUserToken == null || appUserToken.getToken() == null || appUserToken.getToken().length() == 0) {
            appUserToken = jwtUtil.createToken("ApiClientApp");
        }
        return appUserToken;
    }

}

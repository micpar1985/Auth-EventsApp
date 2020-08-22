package com.event_app.auth.api;

import java.net.URI;
import java.nio.charset.Charset;
import com.event_app.auth.domain.Customer;
import com.event_app.auth.domain.Token;
import com.event_app.auth.util.JWTHelper;
import com.event_app.auth.util.JWTUtil;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import com.event_app.auth.domain.CustomerFactory;


@RestController
@RequestMapping("/register")
public class RegisterAPI {

    public static Token appUserToken;
    static JWTUtil jwtUtil = new JWTHelper();

    @PostMapping
    public ResponseEntity<?> registerCustomer(@RequestBody Customer newCustomer, UriComponentsBuilder uri) {
        if (newCustomer.getId() != 0 || newCustomer.getName() == null || newCustomer.getEmail() == null) {
            // Reject we'll assign the customer id
            return ResponseEntity.badRequest().build();
        }
        String json_string = CustomerFactory.getCustomerAsJSONString(newCustomer);
        postNewCustomerToCustomerAPI(json_string);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCustomer.getId()).toUri();
        ResponseEntity<?> response = ResponseEntity.created(location).build();
        return response;
    }

    private void postNewCustomerToCustomerAPI(String json_string) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String customersUrl = "http://localhost:8080/api/customers";
            Customer customer = CustomerFactory.getCustomer(json_string);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAppUserToken().getToken());
            //HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            ResponseEntity<Customer> response = restTemplate.exchange(customersUrl, HttpMethod.POST, new HttpEntity<>(customer, headers), Customer.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                try {
                    System.out.println("Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
                    JSONObject jsonObject = new JSONObject(response.getBody());
                } catch (JSONException e) {
                    throw new RuntimeException("JSONException occurred");
                }
            }
            Customer newCustomer = response.getBody();
        } catch (final HttpClientErrorException httpClientErrorException) {
            System.out.println("httpClientErrorException");
        } catch (HttpServerErrorException httpServerErrorException) {
            System.out.println("httpServerErrorException");
        } catch (Exception exception) {
            System.out.println("Other Exception");
        }

    }

    HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    public static Token getAppUserToken() {
        if (appUserToken == null || appUserToken.getToken() == null || appUserToken.getToken().length() == 0) {
            appUserToken = jwtUtil.createToken("ApiClientApp");
        }
        return appUserToken;
    }
}

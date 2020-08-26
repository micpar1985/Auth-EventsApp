package com.event_app.auth.api;

import java.net.URI;

import com.event_app.auth.domain.Customer;
import com.event_app.auth.domain.Token;
import com.event_app.auth.util.JWTHelper;
import com.event_app.auth.util.JWTUtil;
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
            String apiHost = System.getenv("API_HOST");
            RestTemplate restTemplate = new RestTemplate();
            String customersUrl = "http://" + apiHost + "/api/customers";
            Customer customer = CustomerFactory.getCustomer(json_string);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + getAppUserToken().getToken());
            ResponseEntity<Customer> response = restTemplate.exchange(customersUrl, HttpMethod.POST, new HttpEntity<>(customer, headers), Customer.class);
        } catch (final HttpClientErrorException httpClientErrorException) {
            System.out.println("httpClientErrorException");
        } catch (HttpServerErrorException httpServerErrorException) {
            System.out.println("httpServerErrorException");
        } catch (Exception exception) {
            System.out.println("Other Exception");
        }
    }

    public static Token getAppUserToken() {
        if (appUserToken == null || appUserToken.getToken() == null || appUserToken.getToken().length() == 0) {
            appUserToken = jwtUtil.createToken("ApiClientApp");
        }
        return appUserToken;
    }
}

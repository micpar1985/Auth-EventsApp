package com.event_app.auth.api;

import java.net.URI;
import java.nio.charset.Charset;

import com.event_app.auth.domain.Customer;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.event_app.auth.domain.CustomerFactory;

@RestController
@RequestMapping("/register")
public class RegisterAPI {

    @PostMapping
    public ResponseEntity<?> registerCustomer(@RequestBody Customer newCustomer, UriComponentsBuilder uri) {
        if (newCustomer.getId() != 0 || newCustomer.getName() == null || newCustomer.getEmail() == null) {
            // Reject we'll assign the customer id
            return ResponseEntity.badRequest().build();
        }

        String json_string = CustomerFactory.getCustomerAsJSONString(newCustomer);

        postNewCustomerToCustomerAPI(json_string);

        // old code that calls repository directly
        // newCustomer = repo.save(newCustomer);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newCustomer.getId()).toUri();
        ResponseEntity<?> response = ResponseEntity.created(location).build();
        return response;
    }

    private void postNewCustomerToCustomerAPI(String json_string) {
        RestTemplate restTemplate = new RestTemplate();
        String customersUrl = "http://localhost:8080/api/customers";
        Customer customer = CustomerFactory.getCustomer(json_string);
        ResponseEntity<Customer> response = restTemplate.exchange
                (customersUrl, HttpMethod.POST, new HttpEntity<Customer>(createHeaders(customer.getName(), customer.getPassword())), Customer.class);
        Customer newCustomer = response.getBody();

        /*try {
            URL url = new URL("http://localhost:8080/api/customers");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            Token token = Authenticator.getAppUserToken();
            conn.setRequestProperty("authorization", "Bearer " + token.getToken());
            // conn.setRequestProperty("tokencheck", "false");

            OutputStream os = conn.getOutputStream();
            os.write(json_string.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }*/

    }

    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

}

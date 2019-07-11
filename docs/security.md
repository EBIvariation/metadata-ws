# Security

This application uses Spring OAuth2 and Keycloak authorization to secure its REST APIs. The mappings from OAuth2 terminology to our implementation are:
   - Authorization server: Keycloak server
   - Resource server: Metadata Application
   - Resource owner: Keycloak users with allowed role
   - Client: Application calling Metadata REST APIs

Endpoints

- No restrictions are applied on the `/` and `/swagger-ui.html` endpoints, which can be accessed by anonymous users, for example via http://localhost:8085/ and http://localhost:8085/swagger-ui.html
- Among all other endpoints:
     - Read-only (GET) operations can be issued by anonymous user.
     - Update operations (POST, PUT, PATCH, DELETE) can be performed only by users with the "SERVICE_OPERATOR" role (described below).


##Setup

   - Enter the Keycloak public key value in the `application.properties` file, property _security.oauth2.resource.jwt.key-value_. It should have a value similar to `-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9wxxxxxxxxxxxxxxxxxxxffjZs/D+e9A56XuRJSQ9QIDAQAB\n-----END PUBLIC KEY-----`
   - This can be obtained from Keycloak Realm settings -> Keys -> Public key, at the right-most column.
  
 
##Configuring Keycloak
  
1. Setup and configure Keycloak server with realm, client, role, mapper and user [as described in the official documentation](https://www.keycloak.org/docs/latest/getting_started/index.html)
2. Consider following are configured in Keycloak as an example
     - Role: “SERVICE_OPERATOR”. This role name is mandatory. It could be either client role or realm role.
     - User name Mapper: Keycloak returns access token in JWT. Logged in username is returned in claim “preferred\_username” in access token. Spring Security OAuth2 resource Server expects username in claim “user\_name”. Hence a mapper to map logged in username to a new claim named user_name is required
     - 'Redirect URL' in client settings should point to where the metadata API is running


## Testing Keycloak
1. Get authorization token from the Keycloak server using Postman (for testing)
2. In order to send the token
    - 2.1. From Swagger: click the `Authorize` button and enter "Bearer <token obtained>"
    - 2.2. From a REST client: include header "Authorization" with value "Bearer <token obtained>"

## Security

Metadata application uses Spring OAuth2 and Keycloak authorization to secure REST APIs. W.r.t OAuth2 standards mappings are
   - Authorization server: Keycloak server
   - Resource server: Metadata Application
   - Resource owner: Keycloak users with allowed role
   - Client: Application calling Metadata REST APIs

Endpoints

   - / and /swagger-ui.html which can be accessed by ananymous user. Ex: http://localhost:8085/ and http://localhost:8085/swagger-ui.html
   - All other endpoints: 
         - Read(GET) operation can be issued by any authenticated user with keycloak. The user can have any role assigned
        - Update operation (POST, PUT, PATCH, DELETE) can be done by only users with “SERVICE_OPERATOR” role(described later)


** Building metadata API application **

   - Enter keycloak public key value in Application.properties file “security.oauth2.resource.jwt.key-value”. It should have a value like “-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9wxxxxxxxxxxxxxxxxxxxffjZs/D+e9A56XuRJSQ9QIDAQAB\n-----END PUBLIC KEY-----“
   - This can be obtained from Keycloak Realm settings->Keys->Public key at the right-most column
  
 
** Configuring keycloak **
  
1. Setup and configure keycloak server with realm, client, role, mapper and user
2. Consider following are configured in keycloak as an example
     - Keycloak server running at: “http://localhost:8080” 
     - Relam: “securemetadata”
     - Client id: “secure-client”
     - Role: “SERVICE_OPERATOR”. This role name is mandatory. It could be either client role or realm role.
     - User name Mapper: Keycloak returns access token in JWT. Logged in username is returned in claim “preferred\_username” in access token. Spring Security OAuth2 resource Server expects username in claim “user\_name”. Hence a mapper to map logged in username to a new claim named user_name is required
     - User: “svc_opr”
     - Redirect URL in client settings: “http://localhost:8085” (Metadata application)
3. Get all security endpoints from OpenID configuration URL "http://localhost:8880/auth/realms/securemetadata/.well-known/openid-configuration". 
      - It should contain several endpoints and other information, which should appear like
      
> "issuer": "http://localhost:8880/ega/ampt2d/auth/realms/securemetadata" <br>
> “authorization_endpoint": "http://localhost:8880/ega/ampt2d/auth/realms/securemetadata/protocol/openid-connect/auth" <br>
> "token_endpoint": "http://localhost:8880/ega/ampt2d/auth/realms/securemetadata/protocol/openid-connect/token" <br>
> "token\_introspection_endpoint": "http://localhost:8880/ega/ampt2d/auth/realms/securemetadata/protocol/openid-connect/token/introspect" <br>
> "userinfo_endpoint": "http://localhost:8880/ega/ampt2d/auth/realms/securemetadata/protocol/openid-connect/userinfo"

   
** Get authorization token from keycloak server using postman **

1. Select Authorization tab->Type Auth2.0->Click Get new access token
2. Set following entries considering above keycloak configuration
      - Grant Type: Authorization code
      - Callback URL: http://localhost:8085
      - Auth URL: It is authorization_endpoint received earlier. http://localhost:8880/ega/ampt2d/auth/realms/securemetadata/protocol/openid-connect/auth
      - Access token URL: It is token_endpoint received. http://localhost:8880/ega/ampt2d/auth/realms/securemetadata/protocol/openid-connect/token
      - Client ID: secret-client
      - Client secret: 6axxxxx76-111-9cyyyb-6ezzzzd80
      - Scope: openid
      - State: Something like 12345
      - Client Authentication: Send client credentials in body
3. Clicking “Get new access token” will throw keycloak dialog to enter username and password. Enter user and password to get required access
4. The output should have 
      - lengthy “access token” like  “eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkxxxxxxxxxvYW1wdDJkL2F1dGgvcmV”
      - Apart from this it will also have Token type, expires in, refresh token, id token, scope
5. Sending token
      - In Swagger, Click Authorize button and enter “Bearer <token obtained>”
      - In REST client include header “Authorization” with “Bearer <token obtained>”
   
   
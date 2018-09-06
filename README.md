# Metadata API
Metadata API for projects such as AMP T2D, EGA, EVA etc.

### OAuth2 Authenticated

The authentication is provided by Keycloak Server.
To perform any valid authenticated request please follow the below steps,

#### What you need are the following:

* Client set up in Keycloak server
* Client ID 
* Client Secret
* User set up for the client

#### Requesting Authorization
``` 
curl --data "grant_type=password&amp;client_id=<client_id>&amp;client_secret=<client_secret>&amp;username=<username>&amp;password
=<password>" https://www.ebi.ac
.uk/ega/ampt2d/auth/realms/Ampt2d/protocol/openid-connect/token 
```

You should get something like this:

``` {  
“access_token”:“XXXXX”,
“expires_in”:300,
“id_token”:“XXXXX”,
“refresh_token”:“XXXXX”,
“token_type”:“Bearer”
} 
``` 

You now have an access token you can use in your rest method calls.


#### Accessing the endpoints
  Root url / is a unsecured service that can be accessed without token , i.e.

```
curl <host>:<port>/

```

  The rest of the services require a oauth2 authentication token. And to access one of the secure rest services we send the access_token like this

```
curl -H "Authorization: Bearer 27647b94-1f9f-4945-ae8f-6521d48fdcad" <host>:<port>/studies/
```

### Sending tokens in swagger

Please pass the token in Authorize box value as "Bearer \<Token>"

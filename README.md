# Metadata API

Metadata API for projects such as AMP T2D, EGA, EVA etc.

### OAuth2 authenticated rest content server

The authentication is provided by google api.

To perform any valid authenticated request please follow the below steps,

#### What you need are the following:
* Sample app setup in - https://console.developers.google.com/
* Client ID 
* Client Secret 
* Scopes –  the scopes define what access you will receive.  you can have more then one just put a space between them. 

eg : used for testing ,
* Client Id     - 548139723323-g70r2bllnlkcgcq85vo5kjshkegitomk.apps.googleusercontent.com
* Client Secret - zBTsE__1l35F6qupvxvENfvV
* Scopes        - openid

#### Requesting Authorization
   Now replace the values needed in the following link and put it in a web browser.
   
   

``` https://accounts.google.com/o/oauth2/auth?client_id=[Application Client Id]&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=[Scopes]&response_type=code ```

#### Exchanging Authentication code
   You should get the standard request for authentication.   Once you have accepted authentication copy the Authentication code.   Take the following code replace the values as needed.

```
curl \
–request POST \
–data “code=[Authentication code from authorization link]&client_id=[Application Client 
Id]&client_secret=[Application Client Secret]&redirect_uri=urn:ietf:wg:oauth:2.0:oob&grant_type=authorization_code” \
https://accounts.google.com/o/oauth2/token  
```

You should get something like this:

``` {  
“access_token”:“XXXXX”,
“expires_in”:3600,
“id_token”:“XXXXX”,
“refresh_token”:“XXXXX”,
“token_type”:“Bearer”
} 
``` 

You now have an access token you can use in your Google API call.

#### Use Refresh Token

If your access token expires you can use the following command to refresh it using the Refresh token.

```
curl \
–request POST \
–data ‘client_id=[Application Client Id]&client_secret=[Application Client Secret]&refresh_token=[Refresh token granted by second step]&grant_type=refresh_token’ \
https://accounts.google.com/o/oauth2/token
```

The response will be slightly different this time.  You wont get a new Refresh token.

``` 
{
“access_token” : “XXXXX”,
“expires_in” : 3600,
“id_token” : “xxxxx”,
“token_type” : “Bearer”
}
```

#### Accessing the endpoints
  Root url / is a unsecured service that can be accesed with a get request, i.e.

```
curl <host>:<port>/

```

  The rest of the services require a oauth2 authentication token. And to access one of the secure rest services we send the access_token like this

```
curl -H "Authorization: Bearer 27647b94-1f9f-4945-ae8f-6521d48fdcad" <host>:<port>/studies/
```

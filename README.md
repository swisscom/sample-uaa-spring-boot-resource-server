# Sample Spring boot OAuth2/OIDC resource server for UAA
Demo app that acts as OAuth2 resource server, meaning it offers a stateless API and is protected by an OAuth2 JWT access token verification.
It's mostly used in conjunction with a javascript frontend that obtains the OAuth2 JWT access token using the `implicit` flow and then uses this API as its backend.

It is primarily designed to run on CloudFoundry, since it expects the OAuth2 configuration to be passed in `VCAP_SERVICES`.

## How it works
The client invokes the backend API with the HTTP header `Authorization: Bearer <JWT access token>`.
The backend extracts this JWT token and validates it on the OAuth2 IDP. See the comments in the code for details.

It was tested with [UAA](https://github.com/cloudfoundry/uaa) acting as OAuth2/OIDC provider, but it should work with every OIDC provider.

## Configure, deploy and test
The app expects a `VCAP_SERVICES` env variable containing a service with a tag `oauth2` so make sure it is set when running the app.

When deploying to CloudFoundry, one can use a [User provided service instance](https://docs.cloudfoundry.org/devguide/services/user-provided.html) to achieve this.

Full example:
```
# compile the app
mvn clean package

# adjust ALLOWED_CORS_ORIGIN in manifest.yml if you want to use a browser client, then push
cf push sample --random-route --no-start -p target/sample-uaa-spring-boot-resource-server-0.0.1-SNAPSHOT.jar

# Now create the user provided service which will be provided to the app in VCAP_SERVICES.
# The client specified here must be created manually beforehand on the OAuth2 provider.
CREDENTIALS='{"userInfoEndpoint": "<uaa-url>/userinfo", "introspectEndpoint": "<uaa-url>/introspect", "clientId": "<client-id>", "clientSecret": "<client-secret>"}'
cf create-user-provided-service OAUTH2-CLIENT -p $CREDENTIALS -t oauth2

# Bind & start the app to make the service instance available
cf bind-service sample OAUTH2-CLIENT
cf start sample
```

You can now speak to the API, providing a valid token:
```
TOKEN=obtain token from IDP
curl -v https://<path-to-your-app>/env -H "Authorization: Bearer $TOKEN"
```

And the app will return the user attributes it received from the IDP.

For a full demo, you can deploy a client sample app (see below) that will act as the client for you.


## Sample overview
### Authorization code
- Service provider (Spring boot): https://github.com/swisscom/sample-uaa-spring-boot-service-provider
- Service provider (Ruby): https://github.com/swisscom/sample-uaa-ruby-service-provider

### Implicit flow & Client Credentials
- Client (VueJS): https://github.com/swisscom/sample-uaa-vue-client
- Client (React & Redux):https://github.com/swisscom/sample-uaa-react-redux-client
- Client (AngularJS): https://github.com/swisscom/sample-uaa-angular-client

- Resource Server (Spring boot): https://github.com/swisscom/sample-uaa-spring-boot-resource-server"
- Resource Server (Ruby): https://github.com/swisscom/sample-uaa-ruby-resource-server
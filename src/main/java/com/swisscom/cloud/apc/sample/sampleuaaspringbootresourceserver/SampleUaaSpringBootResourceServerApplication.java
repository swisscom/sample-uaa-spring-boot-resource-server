package com.swisscom.cloud.apc.sample.sampleuaaspringbootresourceserver;

import com.jayway.jsonpath.JsonPath;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@SpringBootApplication
@Controller
public class SampleUaaSpringBootResourceServerApplication {
    public static void main(String[] args) {
        String vcapServices = System.getenv().get("VCAP_SERVICES");
        Optional<Map<String, Object>> maybeCredentials = parseOAuth2Credentials(vcapServices);
        System.out.println(maybeCredentials.orElseThrow(() -> new RuntimeException("Oauth2 credentials not found in VCAP_SERVICES")));

        Map<String, Object> credentials = maybeCredentials.get();
        HashMap<String, Object> props = new HashMap<>();

        // these properties in combination with @EnableResourceServer in SecurityConfiguration
        // enables the spring boot app to act as resource server, checking each token on either /userinfo or /introspect
        // (/check_token replacement)
        // be sure to include all dependencies in build.gradle.
        props.put("security.oauth2.client.client-id", credentials.get("clientId"));
        props.put("security.oauth2.client.client-secret", credentials.get("clientSecret"));
        props.put("security.oauth2.client.grant-type", credentials.get("grantTypes"));
        props.put("security.oauth2.resource.user-info-uri", credentials.get("userInfoEndpoint"));
        props.put("security.oauth2.resource.token-info-uri", credentials.get("introspectEndpoint"));

        // use this flag to decide whether you want to use the app to verify the token using
        // a the /introspect endpoint (prefer-token-info=true) or /userinfo (prefer-token-info=false):
        //
        // use /introspect (prefer-token-info=true) if you only want to check the token.
        // and not need user attributes, since this endpoints returns only these attributes:
        // https://docs.cloudfoundry.org/api/uaa/version/4.27.0/index.html#introspect-token
        // further, spring boot's DefaultUserAuthenticationConverter does not use most fields of the response,
        // so you actually only get the username attribute, unless you write your own converter.
        // the /introspect endpoint is actually doing the same as /check_token endpoint, but it is OIDC compatible,
        // so it works with spring.
        //
        // use /userinfo if you want to check the user and all user attributes (including group memberships)
        // https://docs.cloudfoundry.org/api/uaa/version/4.27.0/index.html#user-info
        props.put("security.oauth2.resource.prefer-token-info", "true");

        new SpringApplicationBuilder()
                .sources(SampleUaaSpringBootResourceServerApplication.class)
                .properties(props)
                .run(args);
    }

    public static Optional<Map<String, Object>> parseOAuth2Credentials(String vcapServices) {
        if (vcapServices != null) {
            List<Map<String, Object>> services = JsonPath.parse(vcapServices)
                    .read("$.*.[?(@.credentials)]", List.class);

            return services.stream().filter(o -> {
                Collection<String> tags = (Collection<String>) o.get("tags");
                return (tags != null && tags.contains("oauth2"));
            }).findFirst().map(t -> (Map<String, Object>) t.get("credentials"));

        }
        return Optional.empty();
    }

    @RequestMapping("/env")
    @ResponseBody
    @CrossOrigin(origins = "${ALLOWED_CORS_ORIGIN:example.com}")
    public String env(Principal user) {
        // print user attributes to show what's available to the app is either taken from /userinfo or from /check_token
        // depending on the value security.oauth2.resource.prefer-token-info above
        return userAttributesAsJson(user);
    }

    private String userAttributesAsJson(Principal user) {
        Authentication authentication = ((OAuth2Authentication) user).getUserAuthentication();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(authentication);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

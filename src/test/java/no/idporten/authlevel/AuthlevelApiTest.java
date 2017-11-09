package no.idporten.authlevel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;


public class AuthlevelApiTest {

    private static final String ISSUER = "test_authlevel";
    private static final String AUDIENCE = "https://oidc-ver2.difi.no/idporten-oidc-provider/";
    private static final String TOKEN_ENDPOINT = "https://oidc-ver2.difi.no/idporten-oidc-provider/token";
    private static final String API_ENDPOINT = "https://kontaktinfo-ws-ver2.difi.no/authlevel/rest/v1/sikkerhetsnivaa";
    private static final String SCOPE = "global/idporten.authlevel.read";


    @Test
    public void testApiRequest() throws Exception {

        String assertion = makeJwt();
        TokenResponse tokenResponse = makeTokenRequest(assertion);
        
		
		System.out.println("Making request to "+API_ENDPOINT);
		ApiResponse apiResponse = makeApiRequest("08023549930", tokenResponse.getAccessToken());

        System.out.println("Got response from authlevel api:");
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(apiResponse));

        assertTrue(true);
    }

    public TokenResponse makeTokenRequest(String assertion) throws Exception {

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(TOKEN_ENDPOINT);
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
        nvps.add(new BasicNameValuePair("assertion", assertion));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

        String responseContent = readContentFromResponse(httpResponse);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed to retrieve access_token. Got "
                    +httpResponse.getStatusLine().getStatusCode()
                    +"-response from token endpoint: "+responseContent);
        }

        return new Gson().fromJson(responseContent, TokenResponse.class);

    }

    private ApiResponse makeApiRequest(String ssn, String accessToken) throws Exception {

        String request = new Gson().toJson(new ApiRequest(ssn));

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(API_ENDPOINT);
        httpPost.addHeader("Authorization", "Bearer "+accessToken);
        httpPost.setEntity(new StringEntity(request, ContentType.APPLICATION_JSON));

        CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

        String responseContent = readContentFromResponse(httpResponse);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Api request failed. Got "
                    +httpResponse.getStatusLine().getStatusCode()
                    +"-response from token endpoint: "+responseContent);
        }

        return new Gson().fromJson(responseContent, ApiResponse.class);
    }

    private String makeJwt() throws Exception {

        KeyStore keyStore = loadKeystore();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("buypass", "changeit".toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate("buypass");

        List<Base64> certChain = new ArrayList<>();
        certChain.add(Base64.encode(certificate.getEncoded()));
        JWSHeader jwtHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(certChain).build();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .audience(AUDIENCE)
                .issuer(ISSUER)
                .claim("scope", SCOPE)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date(Clock.systemUTC().millis()))
                .expirationTime(new Date(Clock.systemUTC().millis() + 120000)) // Expiration time is 120 sec.
                .build();

        JWSSigner signer = new RSASSASigner(privateKey);
        SignedJWT signedJWT = new SignedJWT(jwtHeader, claims);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    private String readContentFromResponse(CloseableHttpResponse httpResponse) throws Exception {
        String content = "";
        try {
            HttpEntity entity = httpResponse.getEntity();
            content = new Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A").next();
            EntityUtils.consume(entity);
        } finally {
            httpResponse.close();
        }
        return content;
    }

    private KeyStore loadKeystore() throws Exception {
        InputStream is = AuthlevelApiTest.class.getClassLoader().getResourceAsStream("keystore.jks");

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "changeit".toCharArray());

        return keyStore;
    }

}
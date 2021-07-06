package org.thingsboard.server.dft.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.thingsboard.server.config.ThingsboardSecurityConfiguration;
import org.thingsboard.server.service.security.auth.rest.LoginRequest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BaseAbstractControllerDftTest.class, loader = SpringBootContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Configuration
@ComponentScan({"org.thingsboard.server"})
@WebAppConfiguration
@SpringBootTest()
@Slf4j
public abstract class BaseAbstractControllerDftTest {

    protected String token;
    protected String refreshToken;
    protected String username;
    protected Gson gson;

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        log.info("Executing setup");
        if (this.mockMvc == null) {
            this.mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        }
        gson = new Gson();
        log.info("Executed setup");
    }

    protected void loginUserDftWithParameters(String username, String password) throws Exception {
        loginDft(username, password);
    }

    protected void loginSysAdminDft() throws Exception {
        loginDft("sysadmin@thingsboard.org", "sysadmin");
    }

    protected void loginUserDft() throws Exception {
        loginDft("huytran@gmail.com", "12345678");
    }

    protected void loginDft(String username, String password) throws Exception {
        this.token = null;
        this.refreshToken = null;
        this.username = null;
        LoginRequest loginRequest = new LoginRequest(username, password);
        Gson gson = new Gson();
        String jsonLogin = gson.toJson(loginRequest);

        MvcResult responseLogin = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .content(jsonLogin)).andReturn();
        JsonNode tokenInfo = new ObjectMapper().readValue(responseLogin.getResponse().getContentAsString(), JsonNode.class);
        validateAndSetJwtTokenUserDft(tokenInfo, username);
    }

    protected void validateAndSetJwtTokenUserDft(JsonNode tokenInfo, String username) {
        Assert.assertNotNull(tokenInfo);
        Assert.assertTrue(tokenInfo.has("token"));
        Assert.assertTrue(tokenInfo.has("refreshToken"));
        String token = tokenInfo.get("token").asText();
        String refreshToken = tokenInfo.get("refreshToken").asText();
        validateJwtTokenDft(token, username);
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
    }

    protected void validateJwtTokenDft(String token, String username) {
        Assert.assertNotNull(token);
        Assert.assertFalse(token.isEmpty());
        int i = token.lastIndexOf('.');
        Assert.assertTrue(i > 0);
        String withoutSignature = token.substring(0, i + 1);
        Jwt<Header, Claims> jwsClaims = Jwts.parser().parseClaimsJwt(withoutSignature);
        Claims claims = jwsClaims.getBody();
        String subject = claims.getSubject();
        Assert.assertEquals(username, subject);
    }

    protected void logoutDft() throws Exception {
        this.token = null;
        this.refreshToken = null;
        this.username = null;
    }

    protected <T> T doPostDft(String url, T object, Class<T> responseClass) throws Exception {

        String jsonRequest = gson.toJson(object);

        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonRequest)).andReturn();
        return (new ObjectMapper().readValue(response.getResponse().getContentAsString(), responseClass));
    }

    protected <T> T doDeleteDft(String url, T object, Class<T> responseClass) throws Exception {

        String jsonRequest = gson.toJson(object);

        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonRequest)).andReturn();
        return (new ObjectMapper().readValue(response.getResponse().getContentAsString(), responseClass));
    }

    protected <T> T doDeleteByIdDft(String url, String id, Class<T> responseClass) throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.delete(url + id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        return (new ObjectMapper().readValue(response.getResponse().getContentAsString(), responseClass));
    }
}

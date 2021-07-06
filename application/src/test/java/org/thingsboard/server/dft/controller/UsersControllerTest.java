package org.thingsboard.server.dft.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.thingsboard.server.common.data.audit.AuditLog;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.config.ThingsboardSecurityConfiguration;
import org.thingsboard.server.controller.AbstractControllerTest;
import org.thingsboard.server.dft.common.messageUser.MessageResponseUser;
import org.thingsboard.server.dft.controllers.web.khachhang.dtos.KhachHangDto;
import org.thingsboard.server.dft.controllers.web.users.dtos.UsersDto;
import org.thingsboard.server.service.security.auth.rest.LoginRequest;


public class UsersControllerTest extends AbstractControllerTest {

    private String token;
    private String refreshToken;
    private String username;

    @Test
    public void testCreateUserWhenParameterExactly() throws Exception {
        loginSysAdminDft();

        KhachHangDto client = new KhachHangDto();
        client.setMaKhachHang("testJunit001");
        client.setTenKhachHang("testJunit");
        client.setEmail("testJunit@test.com");
        client.setSoDienThoai("0000123456789");
        client.setNgayBatDau(System.currentTimeMillis());
        client.setGhiChu("");
        client.setActive(true);

        Gson gson = new Gson();
        String jsonClient = gson.toJson(client);

        MvcResult responseOfCreateClient = mockMvc.perform(MockMvcRequestBuilders.post("/api/sys-admin/khach-hang")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonClient)).andReturn();
        KhachHangDto savedClient = new ObjectMapper().readValue(responseOfCreateClient.getResponse().getContentAsString(), KhachHangDto.class);
        Assert.assertNotNull(savedClient.getId());

        logoutDft();

        loginUserDftWithParameters(savedClient.getEmail(), "12345678");

        String description = "{\"description\":\"\"}";
        UsersDto newUser = new UsersDto();
        newUser.setAdditionalInfo(new ObjectMapper().readTree(description));
        newUser.setFirstName("testCreateUser");
        newUser.setLastName("00000000000");
        newUser.setEmail("test_create_user@test.com");
        newUser.setPassword("1234qwer");
        newUser.setEnabled(true);

        String jsonOriginal = "{\"additionalInfo\":{\"description\":\"\"}," +
                "\"firstName\":\"" + newUser.getFirstName() + "\"," +
                "\"email\":\"" + newUser.getEmail() + "\"," +
                "\"lastName\":\"" + newUser.getLastName() + "\"," +
                "\"password\":\"" + newUser.getPassword() + "\"," +
                "\"enabled\":true," +
                "\"roleId\":[]}";

        MvcResult responseOfCreateUser = mockMvc.perform(MockMvcRequestBuilders.post("/api/save-users/create")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonOriginal)).andReturn();
        UsersDto savedUser = new ObjectMapper().readValue(responseOfCreateUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), savedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_002, savedUser.getResponseMessage());

        MvcResult responseOfDeleteUser = mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/" + savedUser.getId().getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        UsersDto deletedUser = new ObjectMapper().readValue(responseOfDeleteUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), deletedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_003, deletedUser.getResponseMessage());

        logoutDft();

        loginSysAdminDft();

        MvcResult responseOfDeleteClient = mockMvc.perform(MockMvcRequestBuilders.delete("/api/sys-admin/khach-hang/" + savedClient.getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), responseOfDeleteClient.getResponse().getStatus());
    }

    @Test
    public void testEditUserWhenParameterExactly() throws Exception {
        loginSysAdminDft();

        KhachHangDto client = new KhachHangDto();
        client.setMaKhachHang("testJunit002");
        client.setTenKhachHang("testJunit2");
        client.setEmail("testJunit2@test.com");
        client.setSoDienThoai("0000123456780");
        client.setNgayBatDau(System.currentTimeMillis());
        client.setGhiChu("");
        client.setActive(true);

        Gson gson = new Gson();
        String jsonClient = gson.toJson(client);

        MvcResult responseOfCreateClient = mockMvc.perform(MockMvcRequestBuilders.post("/api/sys-admin/khach-hang")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonClient)).andReturn();
        KhachHangDto savedClient = new ObjectMapper().readValue(responseOfCreateClient.getResponse().getContentAsString(), KhachHangDto.class);
        Assert.assertNotNull(savedClient.getId());

        logoutDft();

        loginUserDftWithParameters(savedClient.getEmail(), "12345678");

        String description = "{\"description\":\"\"}";
        UsersDto newUser = new UsersDto();
        newUser.setAdditionalInfo(new ObjectMapper().readTree(description));
        newUser.setFirstName("testCreateUser");
        newUser.setLastName("00000000000");
        newUser.setEmail("test_create_user@test.com");
        newUser.setPassword("1234qwer");
        newUser.setEnabled(true);

        String jsonCreate = "{\"additionalInfo\":{\"description\":\"\"}," +
                "\"firstName\":\"" + newUser.getFirstName() + "\"," +
                "\"email\":\"" + newUser.getEmail() + "\"," +
                "\"lastName\":\"" + newUser.getLastName() + "\"," +
                "\"password\":\"" + newUser.getPassword() + "\"," +
                "\"enabled\":true," +
                "\"roleId\":[]}";

        MvcResult responseOfCreateUser = mockMvc.perform(MockMvcRequestBuilders.post("/api/save-users/create")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonCreate)).andReturn();
        UsersDto createdUser = new ObjectMapper().readValue(responseOfCreateUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), createdUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_002, createdUser.getResponseMessage());

        MvcResult responseOfFindUser = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/" + createdUser.getId().getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        UsersDto foundUser = new ObjectMapper().readValue(responseOfFindUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), foundUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_001, foundUser.getResponseMessage());

        foundUser.setFirstName("testEditNameUser");

        String jsonEdit = "{\"id\":{\"entityType\":\""+ foundUser.getId().getEntityType().toString() + "\",\"id\":\"" + foundUser.getId().getId().toString() + "\"}," +
                "\"createdTime\":\"" + foundUser.getCreatedTime() + "\"," +
                "\"additionalInfo\":{\"description\":\"\"}," +
                "\"firstName\":\"" + foundUser.getFirstName() + "\"," +
                "\"email\":\"" + foundUser.getEmail() + "\"," +
                "\"lastName\":\"" + foundUser.getLastName() + "\"," +
                "\"enabled\":\"" + foundUser.isEnabled() + "\"," +
                "\"tenantId\":{\"entityType\":\"" + foundUser.getTenantId().getEntityType().toString() + "\",\"id\":\"" + foundUser.getTenantId().getId().toString() + "\"}," +
                "\"customerId\":{\"entityType\":\"" + foundUser.getCustomerId().getEntityType().toString() + "\",\"id\":\"" + foundUser.getCustomerId().getId().toString() + "\"}," +
                "\"responseCode\":\"" + foundUser.getResponseCode() + "\"," +
                "\"responseMessage\":\"" + foundUser.getResponseMessage() + "\"," +
                "\"roleEntity\":[]," +
                "\"roleId\":[]}";

        MvcResult responseOfEditUser = mockMvc.perform(MockMvcRequestBuilders.post("/api/save-users/edit")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonEdit)).andReturn();
        UsersDto editedUser = new ObjectMapper().readValue(responseOfEditUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), editedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_002, editedUser.getResponseMessage());

        MvcResult responseOfDeleteUser = mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/" + editedUser.getId().getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        UsersDto deletedUser = new ObjectMapper().readValue(responseOfDeleteUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), deletedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_003, deletedUser.getResponseMessage());

        logoutDft();

        loginSysAdminDft();

        MvcResult responseOfDeleteClient = mockMvc.perform(MockMvcRequestBuilders.delete("/api/sys-admin/khach-hang/" + savedClient.getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), responseOfDeleteClient.getResponse().getStatus());
    }

    @Test
    public void testFindUserWhenIdExactly() throws Exception {
        loginUserDft();

        String description = "{\"description\":\"\"}";
        UsersDto newUser = new UsersDto();
        newUser.setAdditionalInfo(new ObjectMapper().readTree(description));
        newUser.setFirstName("testCreateUserForFind");
        newUser.setLastName("00000000001");
        newUser.setEmail("test_create_user_for_find@test.com");
        newUser.setPassword("1234qwer");
        newUser.setEnabled(true);

        String jsonOriginal = "{\"additionalInfo\":{\"description\":\"\"}," +
                "\"firstName\":\"" + newUser.getFirstName() + "\"," +
                "\"email\":\"" + newUser.getEmail() + "\"," +
                "\"lastName\":\"" + newUser.getLastName() + "\"," +
                "\"password\":\"" + newUser.getPassword() + "\"," +
                "\"enabled\":true," +
                "\"roleId\":[]}";

        MvcResult responseOfCreateUser = mockMvc.perform(MockMvcRequestBuilders.post("/api/save-users/create")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonOriginal)).andReturn();
        UsersDto savedUser = new ObjectMapper().readValue(responseOfCreateUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), savedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_002, savedUser.getResponseMessage());

        MvcResult responseOfFindUser = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/" + savedUser.getId().getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        UsersDto foundUser = new ObjectMapper().readValue(responseOfFindUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), foundUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_001, foundUser.getResponseMessage());

        MvcResult responseOfDeleteUser = mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/" + savedUser.getId().getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        UsersDto deletedUser = new ObjectMapper().readValue(responseOfDeleteUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), deletedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_003, deletedUser.getResponseMessage());
    }

    @Test
    public void testDeleteUserWhenIdExactly() throws Exception {
        loginUserDft();

        String description = "{\"description\":\"\"}";
        UsersDto newUser = new UsersDto();
        newUser.setAdditionalInfo(new ObjectMapper().readTree(description));
        newUser.setFirstName("testCreateUserForDelete");
        newUser.setLastName("00000000002");
        newUser.setEmail("test_create_user_for_delete@test.com");
        newUser.setPassword("1234qwer");
        newUser.setEnabled(true);

        String jsonOriginal = "{\"additionalInfo\":{\"description\":\"\"}," +
                "\"firstName\":\"" + newUser.getFirstName() + "\"," +
                "\"email\":\"" + newUser.getEmail() + "\"," +
                "\"lastName\":\"" + newUser.getLastName() + "\"," +
                "\"password\":\"" + newUser.getPassword() + "\"," +
                "\"enabled\":true," +
                "\"roleId\":[]}";

        MvcResult responseOfCreateUser = mockMvc.perform(MockMvcRequestBuilders.post("/api/save-users/create")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)
                .content(jsonOriginal)).andReturn();
        UsersDto savedUser = new ObjectMapper().readValue(responseOfCreateUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), savedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_002, savedUser.getResponseMessage());

        MvcResult responseOfDeleteUser = mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/" + savedUser.getId().getId().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        UsersDto deletedUser = new ObjectMapper().readValue(responseOfDeleteUser.getResponse().getContentAsString(), UsersDto.class);
        Assert.assertEquals(HttpStatus.OK.value(), deletedUser.getResponseCode());
        Assert.assertEquals(MessageResponseUser.OK_003, deletedUser.getResponseMessage());
    }

    @Test
    public void testFindAllUserWhenPageSizeAndPageExactly() throws Exception {
        loginUserDft();

        MvcResult responseOfFindUser = mockMvc.perform(MockMvcRequestBuilders.get("/api/list-users?pageSize=10&page=0")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        PageData<UsersDto> foundUsers = new ObjectMapper().readValue(responseOfFindUser.getResponse().getContentAsString(), PageData.class);

        Assert.assertNotNull(foundUsers);
        Assert.assertEquals(1, foundUsers.getTotalPages());
        Assert.assertEquals(10, foundUsers.getTotalElements());
    }

    @Test
    public void testFindAllAccessHistoryWhenPageSizeAndPageExactly() throws Exception {
        loginUserDft();

        MvcResult responseOfFindLogs = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/logs?pageSize=10&page=0")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        PageData<AuditLog> foundLogs = new ObjectMapper().readValue(responseOfFindLogs.getResponse().getContentAsString(), PageData.class);

        Assert.assertNotNull(foundLogs);
    }

    @Test
    public void testExportExcelWhenParameterExactly() throws Exception {
        loginUserDft();

        MvcResult responseOfFindLogs = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/logs?pageSize=10&page=0")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .header(ThingsboardSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token)).andReturn();
        InputStreamResource blobExcel = new ObjectMapper().readValue(responseOfFindLogs.getResponse().getContentAsString(), InputStreamResource.class);

        Assert.assertNotNull(blobExcel);
    }

    @Test
    public void testLoginSysadminExactly() throws Exception {
        loginSysAdminDft();
    }

    @Test
    public void testLoginTenantExactly() throws Exception {
        loginUserDft();
    }

    private void loginUserDftWithParameters(String username, String password) throws Exception {
        loginDft(username, password);
    }

    private void loginSysAdminDft() throws Exception {
        loginDft("sysadmin@thingsboard.org", "sysadmin");
    }

    private void loginUserDft() throws Exception {
        loginDft("huytran@gmail.com", "12345678");
    }

    private void loginDft(String username, String password) throws Exception {
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

    private void validateAndSetJwtTokenUserDft(JsonNode tokenInfo, String username) {
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

    private void validateJwtTokenDft(String token, String username) {
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
}

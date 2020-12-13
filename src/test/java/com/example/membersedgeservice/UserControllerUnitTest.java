package com.example.membersedgeservice;

import com.example.membersedgeservice.config.JwtTokenUtil;
import com.example.membersedgeservice.model.Comment;
import com.example.membersedgeservice.model.ImgBoardUser;
import com.example.membersedgeservice.model.JwtRequest;
import com.example.membersedgeservice.model.Login;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.junit.Assert.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerUnitTest {
    @Value("${userservice.baseurl}")
    private String userServiceBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    private MockRestServiceServer mockServer;

    private String token;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void initializeMockserver() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        token="";
    }
    @Test
    public void whenRegister_thenReturnUser() throws Exception {

        ImgBoardUser user1 = new ImgBoardUser(
                "testF",
                "testL",
                "test@hotmail.com",
                "test"
        );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/user")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user1))
                );

        mockMvc.perform(post("/user")
                .content(mapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname",is("testF")))
                .andExpect(jsonPath("$.lastname",is("testL")))
                .andExpect(jsonPath("$.email",is("test@hotmail.com")))
                .andExpect(jsonPath("$.password",is("test")));

    }
    @Test
    public void whenLogin_thenReturnToken() throws Exception {

        JwtRequest login1 = new JwtRequest(
                "r0703028@student.thomasmore.be",
                "test"
        );

        ImgBoardUser user = new ImgBoardUser("Robin","Vranckx","r0703028@student.thomasmore.be",new BCryptPasswordEncoder().encode("test") );


        mockServer.expect(ExpectedCount.manyTimes(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/user/"+user.getEmail())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user))
                );

        MvcResult result = mockMvc.perform(post("/login")
                .content(mapper.writeValueAsString(login1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        token = JsonPath.parse(response).read("$.token").toString();
        assertFalse(token.isEmpty());
    }
    @Test
    public void whenUpdatePassword_thenReturnUser() throws Exception {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        ImgBoardUser user = new ImgBoardUser("Robin","Vranckx","r0703028@student.thomasmore.be","test2");
        //whenLogin_thenReturnToken();
        String token = jwtTokenUtil.generateToken(new User(user.getEmail(), user.getPassword(),
                new ArrayList<>()));

        mockServer.expect(ExpectedCount.manyTimes(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/user/"+user.getEmail())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user))
                );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/user")))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user))
                );


        mockMvc.perform(put("/user").header("Authorization", "Bearer " + token)
                .content(mapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Robin")))
                .andExpect(jsonPath("$.lastname", is("Vranckx")))
                .andExpect(jsonPath("$.email",is("r0703028@student.thomasmore.be")))
                .andExpect(jsonPath("$.password",is("test2")));

    }
    @Test
    public void whenDeleteUser_thenReturnStatus() throws Exception {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        ImgBoardUser user = new ImgBoardUser("Robin","Vranckx","r0703028@student.thomasmore.be","test2");
        //whenLogin_thenReturnToken();
        String token = jwtTokenUtil.generateToken(new User(user.getEmail(), user.getPassword(),
                new ArrayList<>()));
        mockServer.expect(ExpectedCount.manyTimes(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/user/"+user.getEmail())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user))
                );

        mockServer.expect(ExpectedCount.manyTimes(),
                requestTo(new URI("http://" + userServiceBaseUrl + "/user/"+user.getEmail())))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK));


        mockMvc.perform(delete("/user/"+user.getEmail()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

    }
}
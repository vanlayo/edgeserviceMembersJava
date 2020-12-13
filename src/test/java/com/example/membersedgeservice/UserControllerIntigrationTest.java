package com.example.membersedgeservice;

import com.example.membersedgeservice.config.JwtTokenUtil;
import com.example.membersedgeservice.model.ImgBoardUser;
import com.example.membersedgeservice.model.JwtRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntigrationTest {
        @Value("${userservice.baseurl}")
        private String userServiceBaseUrl;

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private MockMvc mockMvc;


        private String token;

        private ObjectMapper mapper = new ObjectMapper();


        @Test
        public void whenRegister_thenReturnUser() throws Exception {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            ImgBoardUser user1 = new ImgBoardUser(
                    "testF",
                    "testL",
                    "test@hotmail.com",
                    "test"
            );

            MvcResult result = mockMvc.perform(post("/user")
                    .content(mapper.writeValueAsString(user1))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstname",is("testF")))
                    .andExpect(jsonPath("$.lastname",is("testL")))
                    .andExpect(jsonPath("$.email",is("test@hotmail.com")))
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String encryptedPassword = JsonPath.parse(response).read("$.password").toString();
            assertTrue(encoder.matches("test",encryptedPassword ));

        }
        @Test
        public void whenLogin_thenReturnToken() throws Exception {

            JwtRequest login1 = new JwtRequest(
                    "r0703028@student.thomasmore.be",
                    "test"
            );

            ImgBoardUser user = new ImgBoardUser("Robin","Vranckx","r0703028@student.thomasmore.be",new BCryptPasswordEncoder().encode("test") );


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
            ImgBoardUser user = new ImgBoardUser(
                    "testF",
                    "testL",
                    "test@hotmail.com",
                    "test2"
            );                 //whenLogin_thenReturnToken();
            String token = jwtTokenUtil.generateToken(new User(user.getEmail(), user.getPassword(),
                    new ArrayList<>()));
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();



            MvcResult result=mockMvc.perform(put("/user").header("Authorization", "Bearer " + token)
                    .content(mapper.writeValueAsString(user))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstname", is("testF")))
                    .andExpect(jsonPath("$.lastname", is("testL")))
                    .andExpect(jsonPath("$.email",is("test@hotmail.com")))
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            String encryptedPassword = JsonPath.parse(response).read("$.password").toString();
            assertTrue(encoder.matches("test2",encryptedPassword ));
        }
        @Test
        public void whenDeleteUser_thenReturnStatus() throws Exception {
            JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
            ImgBoardUser user = new ImgBoardUser(
                    "testF",
                    "testL",
                    "test@hotmail.com",
                    "test"
            );            //whenLogin_thenReturnToken();
            String token = jwtTokenUtil.generateToken(new User(user.getEmail(), user.getPassword(),
                    new ArrayList<>()));


            mockMvc.perform(delete("/user/"+user.getEmail()).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

        }
}

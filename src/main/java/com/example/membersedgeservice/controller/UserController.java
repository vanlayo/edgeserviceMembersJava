package com.example.membersedgeservice.controller;

import com.example.membersedgeservice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.membersedgeservice.config.JwtTokenUtil;

import java.util.List;

@RestController
public class UserController {
    @Autowired private ImageLikeController controllerImageLike;
    @Autowired private ImageController imageController;
    @Autowired private CommentController commentController;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${userservice.baseurl}")
    private String userServiceBaseUrl;

    @GetMapping("/user/{email}")
    public ImgBoardUser getUser(@PathVariable String email,@RequestHeader(name="Authorization") String token) {
        ImgBoardUser user = restTemplate.getForObject("http://"+userServiceBaseUrl+" /user/"+email, ImgBoardUser.class);
        user.setPassword(null);
        return user;
    }


        @PostMapping(value ="/user")
    public ImgBoardUser register(@RequestBody ImgBoardUser userRequest){

        ImgBoardUser user = restTemplate.postForObject("http://" + userServiceBaseUrl + "/user",
                userRequest,ImgBoardUser.class);
        return user;
    }
    @PutMapping("/user")
    public ImgBoardUser updateUserPassword(@RequestBody ImgBoardUser updateUser,@RequestHeader(name="Authorization") String token) {
        String jwtToken = token.substring(7);
        String email = jwtTokenUtil.getUsernameFromToken(jwtToken);
        if(updateUser.getEmail().equalsIgnoreCase(email))
        {
            HttpEntity<ImgBoardUser> entity = new HttpEntity<ImgBoardUser>(updateUser);

            HttpEntity<ImgBoardUser> user =restTemplate.exchange("http://" + userServiceBaseUrl + "/user", HttpMethod.PUT,
                    entity,ImgBoardUser.class);
            return user.getBody();
        }else{
            return null;
        }

    }
    @DeleteMapping("/user/{email}")
    public ResponseEntity deleteUser(@PathVariable String email,@RequestHeader(name="Authorization") String token){
        String jwtToken = token.substring(7);
        String emailToken = jwtTokenUtil.getUsernameFromToken(jwtToken);
        if(emailToken.equalsIgnoreCase(email)){
            List<Comment> comments=commentController.getCommentsByUserEmail(email);
            List<Comment> commentsList= comments;
            if(commentsList !=null) {
                for (int i = 0; i < commentsList.size(); i++) {
                    commentController.deleteComment(commentsList.get(i).getKey());
                }
            }

            List<ImageLike> imagesLikesUser=controllerImageLike.getlikesByUserEmail(email);
            if(imagesLikesUser !=null) {
                for (int i = 0; i < imagesLikesUser.size(); i++) {
                    controllerImageLike.deleteLike(imagesLikesUser.get(i).getLikeKey());
                }
            }

            List<Image> images=imageController.getImagesByUserEmail(email);
            if(images !=null){

            for(int i=0;i<images.size();i++){
                imageController.deleteImage(images.get(i).getKey());
            }
            }

            ResponseEntity response =restTemplate.exchange("http://" + userServiceBaseUrl + "/user/"+email, HttpMethod.DELETE,null,
                    String.class);
            return response;
        }else{
            return ResponseEntity.status(403).build();
        }
    }
}

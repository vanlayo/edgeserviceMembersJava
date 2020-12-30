package com.example.membersedgeservice.controller;

import com.example.membersedgeservice.model.FilledImageUserLike;
import com.example.membersedgeservice.model.Image;
import com.example.membersedgeservice.model.ImageLike;
import com.example.membersedgeservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ImageController {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${imageservice.baseurl}")
    private String imageServiceBaseurl;



    @GetMapping("/images/{imageKey}")
    public List<Image> getImagesByImageKey(@PathVariable String imageKey){

        // does image exist?

        // get Image by key
        ResponseEntity<List<Image>> responseEntityReviews =
                restTemplate.exchange("http://" + imageServiceBaseurl + "/images/{imageKey}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Image>>() {
                        }, imageKey);

        return responseEntityReviews.getBody();
    }
    @GetMapping("/images/user/{userEmail}")
    public List<Image> getImagesByUserEmail(@PathVariable String userEmail){

        // does image exist?

        // get images by user
        ResponseEntity<List<Image>> responseEntityReviews =
                restTemplate.exchange("http://" + imageServiceBaseurl + "/images/{userEmail}",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Image>>() {
                        }, userEmail);

        return responseEntityReviews.getBody();
    }
    @PostMapping("/images")
    public Image addImage(@RequestParam String userEmail,
                                       @RequestParam String source,
                                       @RequestParam String description){




        //Make a new image
        Image image =
                restTemplate.postForObject("http://" + imageServiceBaseurl + "/images",
                        new Image(source,userEmail,description),Image.class);

        return image;
    }

    @PutMapping("/images")
    public Image updateImage(@RequestParam String imageKey, @RequestParam String description, @RequestParam String source) {

        //check if likeKey exist
        Image image = restTemplate.getForObject("http://" + imageServiceBaseurl + "/images/" + imageKey,
                Image.class);

        // Update like
        image.setDescription(description);
        image.setSource(source);
        ResponseEntity<Image> responseEntityReview =
                restTemplate.exchange("http://" + imageServiceBaseurl + "/images",
                        HttpMethod.PUT, new HttpEntity<>(image), Image.class);

        Image retrievedImage = responseEntityReview.getBody();


        return retrievedImage;

    }
    @DeleteMapping("/images/{key}")
    public ResponseEntity deleteImage(@PathVariable String key){

        restTemplate.delete("http://" + imageServiceBaseurl + "/images/" + key);

        return ResponseEntity.ok().build();
    }
}
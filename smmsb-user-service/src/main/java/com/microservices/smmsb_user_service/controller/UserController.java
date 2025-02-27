package com.microservices.smmsb_user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.smmsb_user_service.dto.UserDto;
import com.microservices.smmsb_user_service.dto.request.CreateUserRequest;
import com.microservices.smmsb_user_service.dto.request.UpdateUserRequest;
import com.microservices.smmsb_user_service.dto.response.ListResponse;
import com.microservices.smmsb_user_service.dto.response.MessageResponse;
import com.microservices.smmsb_user_service.service.UserService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('SUPERADMIN')")
public class UserController {

   private final UserService userService;

   @Autowired
   public UserController(UserService userService) {
      this.userService = userService;
   }
   
   // Create User
   @PostMapping("/create")
   public MessageResponse createUser(CreateUserRequest createUserRequest) {
      return userService.createUser(createUserRequest);
   }

   // Update User
   @PutMapping("/update/{id}")
   public MessageResponse updateUser(@PathVariable int id, UpdateUserRequest updateUserRequest) {
      return userService.updateUser(id, updateUserRequest);
   }

   // Delete User
   @DeleteMapping("/delete/{id}")
   public MessageResponse deleteUser(@PathVariable int id) {
      return userService.deleteUser(id);
   }

   // Get All Users
   @GetMapping("/get-all")
   public ResponseEntity<ListResponse<EntityModel<UserDto>>> getAllUsers(Pageable pageable, String username,
         String email, String role) {
      ListResponse<UserDto> usersResponse = userService.getAllUsers(pageable, username, email, role);
      List<EntityModel<UserDto>> userResources = usersResponse.getData().stream().map(
         user ->{
            Link selfLink = linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel();
            Link updateLink = linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update");
            Link deleteLink = linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete");
            return EntityModel.of(user, selfLink, updateLink, deleteLink);
            }).toList();

      ListResponse<EntityModel<UserDto>> listResponse = new ListResponse<>(
         userResources,usersResponse.getMessage(),usersResponse.getStatusCode(),usersResponse.getStatus()
      );
      return ResponseEntity.ok(listResponse);  
   }

   // Get User By Id
   @GetMapping("/get-by-id/{id}")
   public UserDto getUserById(@PathVariable int id) {
      return userService.getUserById(id);
   }
   
}

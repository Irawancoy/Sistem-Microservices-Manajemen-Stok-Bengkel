package com.microservices.smmsb_user_service.utils;

import org.springframework.data.jpa.domain.Specification;

import com.microservices.smmsb_user_service.model.User;

public class UserSpecifications {

   public static Specification<User> hasUsername(String username) {
       return (root, query, criteriaBuilder) ->
               criteriaBuilder.equal(root.get("username"), username);
   }

   public static Specification<User> hasEmail(String email) {
      return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), email);
   }
   
   public static Specification<User> hasRole(String role) {
       return (root, query, criteriaBuilder) ->
               criteriaBuilder.equal(root.get("role"), role);
   }

}
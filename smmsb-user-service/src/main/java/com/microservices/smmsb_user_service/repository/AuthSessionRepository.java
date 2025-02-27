package com.microservices.smmsb_user_service.repository;

import com.microservices.smmsb_user_service.model.AuthSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthSessionRepository extends CrudRepository<AuthSession, String> {
}

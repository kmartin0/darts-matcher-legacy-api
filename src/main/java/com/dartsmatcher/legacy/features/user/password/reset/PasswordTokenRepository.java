package com.dartsmatcher.legacy.features.user.password.reset;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordTokenRepository extends MongoRepository<PasswordToken, Long> {

	Optional<PasswordToken> findByToken(UUID token);

}

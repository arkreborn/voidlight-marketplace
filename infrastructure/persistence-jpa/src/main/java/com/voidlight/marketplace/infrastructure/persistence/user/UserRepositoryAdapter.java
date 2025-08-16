package com.voidlight.marketplace.infrastructure.persistence.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.voidlight.marketplace.domain.user.User;
import com.voidlight.marketplace.domain.user.UserRepository;
import com.voidlight.marketplace.infrastructure.persistence.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}

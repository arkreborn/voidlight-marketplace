package com.voidlight.marketplace.infrastructure.persistence.user.mapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.voidlight.marketplace.domain.user.Role;
import com.voidlight.marketplace.domain.user.User;
import com.voidlight.marketplace.infrastructure.persistence.user.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(toRolesString(user.getRoles()))")
    UserEntity toEntity(User user);

    @Mapping(target = "roles", expression = "java(toRoleSet(entity.getRoles()))")
    User toDomain(UserEntity entity);

    default String toRolesString(Set<Role> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    default Set<Role> toRoleSet(String roles) {
        if (roles == null || roles.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(roles.split(","))
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}

package com.example.diet.user.application.assembler;

import com.example.diet.user.api.response.UserResponse;
import com.example.diet.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户装配器
 * 负责领域对象与响应 DTO 之间的转换
 */
@Mapper(componentModel = "spring")
public interface UserAssembler {

    @Mapping(target = "id", expression = "java(user.getId().getValue())")
    @Mapping(target = "username", expression = "java(user.getUsername().value())")
    @Mapping(target = "email", expression = "java(user.getEmail() != null ? user.getEmail().value() : null)")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "status", expression = "java(user.getStatus().getValue())")
    UserResponse toResponse(User user);
}

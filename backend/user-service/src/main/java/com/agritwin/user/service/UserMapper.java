package com.agritwin.user.service;

import com.agritwin.user.dto.UserResponse;
import com.agritwin.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}

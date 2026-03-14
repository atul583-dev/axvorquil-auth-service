package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.ApiResponse;
import com.axvorquil.auth.dto.UpdateRoleRequest;
import com.axvorquil.auth.dto.UserDto;
import com.axvorquil.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users — admin only: list all staff accounts */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserDto>> listAll() {
        return ApiResponse.success("Users retrieved", userService.listAll());
    }

    /** PUT /api/users/{id}/role — admin only: change a user's clinic role */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest req) {
        return ApiResponse.success("Role updated", userService.updateRole(id, req.getRole()));
    }

    /** GET /api/users/me — any authenticated user: own profile with role */
    @GetMapping("/me")
    public ApiResponse<UserDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success("User profile", userService.getByEmail(userDetails.getUsername()));
    }
}

package com.finance.dashboard;

import com.finance.dashboard.dto.request.CreateUserRequest;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.exception.DuplicateResourceException;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserServiceImpl userService;

    @Test
    @DisplayName("createUser — happy path saves and returns UserResponse")
    void createUser_validRequest_returnsUserResponse() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("john");
        req.setEmail("john@test.com");
        req.setPassword("secret123");
        req.setRole(Role.ANALYST);

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(1L).username(u.getUsername()).email(u.getEmail())
                    .password(u.getPassword()).role(u.getRole()).active(true).build();
            return u;
        });

        UserResponse resp = userService.createUser(req);

        assertThat(resp.getUsername()).isEqualTo("john");
        assertThat(resp.getRole()).isEqualTo(Role.ANALYST);
        assertThat(resp.isActive()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser — duplicate username throws DuplicateResourceException")
    void createUser_duplicateUsername_throwsDuplicateResourceException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("admin");
        req.setEmail("other@test.com");
        req.setPassword("secret");
        req.setRole(Role.VIEWER);

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("admin");
    }

    @Test
    @DisplayName("createUser — duplicate email throws DuplicateResourceException")
    void createUser_duplicateEmail_throwsDuplicateResourceException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setEmail("taken@test.com");
        req.setPassword("secret");
        req.setRole(Role.VIEWER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@test.com");
    }
}

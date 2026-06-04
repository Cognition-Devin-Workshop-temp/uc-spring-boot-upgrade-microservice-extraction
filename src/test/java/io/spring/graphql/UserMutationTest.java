package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  @InjectMocks private UserMutation userMutation;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "encoded_pass", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password123")
            .build();
    User newUser = new User("new@test.com", "newuser", "enc", "", "");
    when(userService.createUser(any())).thenReturn(newUser);

    var result = userMutation.createUser(input);

    assertNotNull(result);
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(newUser, result.getLocalContext());
  }

  @Test
  void should_return_error_when_create_user_with_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("bad").username("").password("").build();
    when(userService.createUser(any()))
        .thenThrow(new ConstraintViolationException(new HashSet<>()));

    var result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_login_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password", "encoded_pass")).thenReturn(true);

    var result = userMutation.login("password", "test@test.com");

    assertNotNull(result);
    assertTrue(result.getData() instanceof UserPayload);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_fail_login_with_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", "encoded_pass")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_fail_login_with_nonexistent_email() {
    when(userRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("password", "no@test.com"));
  }

  @Test
  void should_update_user_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("updated")
            .email("updated@test.com")
            .bio("new bio")
            .build();

    var result = userMutation.updateUser(input);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_update_user_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(() -> "ROLE_ANONYMOUS")));

    UpdateUserInput input = UpdateUserInput.newBuilder().username("x").build();
    var result = userMutation.updateUser(input);

    assertNull(result);
  }
}

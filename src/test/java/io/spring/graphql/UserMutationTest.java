package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.HashSet;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
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
    user = new User("test@example.com", "testuser", "encodedpassword", "bio", "image");
  }

  @Test
  void should_create_user_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@example.com")
            .username("newuser")
            .password("password123")
            .build();
    User newUser = new User("new@example.com", "newuser", "encoded", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(UserPayload.class, result.getData());
    assertEquals(newUser, result.getLocalContext());
  }

  @Test
  void should_return_errors_when_create_user_with_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("").username("").password("").build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException(new HashSet<>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNull(result.getLocalContext());
  }

  @Test
  void should_login_success() {
    when(userRepository.findByEmail(eq("test@example.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("password"), eq("encodedpassword"))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@example.com");

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_invalid_authentication_when_login_with_wrong_password() {
    when(userRepository.findByEmail(eq("test@example.com"))).thenReturn(Optional.of(user));
    when(encryptService.matches(eq("wrong"), eq("encodedpassword"))).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@example.com"));
  }

  @Test
  void should_throw_invalid_authentication_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail(eq("notfound@example.com"))).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "notfound@example.com"));
  }

  @Test
  void should_update_user_success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("updateduser")
            .email("updated@example.com")
            .bio("new bio")
            .image("new-image")
            .password("newpass")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  void should_return_null_when_update_user_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    UpdateUserInput input = UpdateUserInput.newBuilder().username("updated").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }

  @Test
  void should_return_null_when_authentication_principal_is_null() {
    TestingAuthenticationToken authWithNullPrincipal = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(authWithNullPrincipal);

    UpdateUserInput input = UpdateUserInput.newBuilder().username("updated").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }
}

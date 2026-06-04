package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, "https://default-image.png", passwordEncoder);
  }

  @Test
  void should_create_user_success() {
    when(passwordEncoder.encode(eq("password"))).thenReturn("encoded");
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    User user = userService.createUser(param);
    assertNotNull(user);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("encoded", user.getPassword());
    assertEquals("https://default-image.png", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user_success() {
    User existingUser = new User("old@test.com", "oldname", "oldpass", "oldbio", "oldimg");

    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newname")
            .password("newpass")
            .bio("newbio")
            .image("newimg")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);
    assertEquals("new@test.com", existingUser.getEmail());
    assertEquals("newname", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_update_user_with_empty_fields() {
    User existingUser = new User("old@test.com", "oldname", "oldpass", "oldbio", "oldimg");
    UpdateUserParam param = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);
    verify(userRepository).save(existingUser);
  }

  @Test
  void should_construct_update_user_command() {
    User user = new User("a@b.com", "user", "pass", "", "");
    UpdateUserParam param = UpdateUserParam.builder().email("x@y.com").build();
    UpdateUserCommand cmd = new UpdateUserCommand(user, param);
    assertEquals(user, cmd.getTargetUser());
    assertEquals(param, cmd.getParam());
  }

  @Test
  void should_construct_register_param() {
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");
    assertEquals("test@test.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password", param.getPassword());
  }

  @Test
  void should_construct_update_user_param_with_defaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_construct_update_user_param_with_values() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("e@mail.com")
            .username("user")
            .password("pass")
            .bio("bio")
            .image("img")
            .build();
    assertEquals("e@mail.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("pass", param.getPassword());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }
}

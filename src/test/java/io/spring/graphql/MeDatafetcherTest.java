package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_me_success() {
    UserData userData = new UserData(user.getId(), "test@test.com", "testuser", "bio", "image");
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    var result = meDatafetcher.getMe("Token mytoken", dfe);

    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(() -> "ROLE_ANONYMOUS")));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    var result = meDatafetcher.getMe("Token xxx", dfe);

    assertNull(result);
  }

  @Test
  void should_throw_when_user_not_found_in_db() {
    when(userQueryService.findById(user.getId())).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);

    assertThrows(ResourceNotFoundException.class, () -> meDatafetcher.getMe("Token mytoken", dfe));
  }

  @Test
  void should_get_user_payload_user() {
    when(jwtService.toToken(user)).thenReturn("jwt-token");

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    var result = meDatafetcher.getUserPayloadUser(dfe);

    assertNotNull(result);
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("jwt-token", result.getData().getToken());
  }
}

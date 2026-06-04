package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.types.UserPayload;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "", "");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_me_success() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    UserData userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), "", "");
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token mytoken123", dfe);

    assertNotNull(result);
    assertEquals(user.getEmail(), result.getData().getEmail());
    assertEquals(user.getUsername(), result.getData().getUsername());
    assertEquals("mytoken123", result.getData().getToken());
  }

  @Test
  void should_return_null_when_anonymous() {
    AnonymousAuthenticationToken anonymous =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonymous);
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);

    assertNull(meDatafetcher.getMe("Token mytoken", dfe));
  }

  @Test
  void should_throw_not_found_when_user_missing() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(userQueryService.findById(user.getId())).thenReturn(Optional.empty());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);

    assertThrows(
        ResourceNotFoundException.class, () -> meDatafetcher.getMe("Token mytoken", dfe));
  }

  @Test
  void should_get_user_payload_user() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(user)).thenReturn("jwt-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dfe);

    assertNotNull(result);
    assertEquals(user.getEmail(), result.getData().getEmail());
    assertEquals("jwt-token", result.getData().getToken());
  }
}

package io.spring.api.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

  @Mock private UserRepository userRepository;
  @Mock private JwtService jwtService;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private JwtTokenFilter jwtTokenFilter;

  @BeforeEach
  void setUp() {
    jwtTokenFilter = new JwtTokenFilter();
    ReflectionTestUtils.setField(jwtTokenFilter, "userRepository", userRepository);
    ReflectionTestUtils.setField(jwtTokenFilter, "jwtService", jwtService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_set_authentication_with_valid_token() throws Exception {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    when(request.getHeader("Authorization")).thenReturn("Token valid-jwt-token");
    when(jwtService.getSubFromToken("valid-jwt-token")).thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_not_set_authentication_when_no_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_not_set_authentication_when_malformed_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("InvalidHeaderNoSpace");

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_not_set_authentication_when_user_not_found() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token valid-jwt-token");
    when(jwtService.getSubFromToken("valid-jwt-token")).thenReturn(Optional.of("user-id"));
    when(userRepository.findById("user-id")).thenReturn(Optional.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_not_override_existing_authentication() throws Exception {
    User existingUser = new User("existing@test.com", "existing", "pass", "", "");
    UsernamePasswordAuthenticationToken existingAuth =
        new UsernamePasswordAuthenticationToken(existingUser, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    User newUser = new User("new@test.com", "newuser", "pass", "", "");
    when(request.getHeader("Authorization")).thenReturn("Token valid-jwt-token");
    when(jwtService.getSubFromToken("valid-jwt-token")).thenReturn(Optional.of(newUser.getId()));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    assertEquals(
        existingUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    verify(filterChain).doFilter(request, response);
  }
}

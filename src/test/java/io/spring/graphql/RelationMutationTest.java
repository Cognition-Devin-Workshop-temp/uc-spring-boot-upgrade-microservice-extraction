package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
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
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  @InjectMocks private RelationMutation relationMutation;

  private User user;
  private User target;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    target = new User("target@test.com", "targetuser", "password", "bio2", "image2");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_follow_user_success() {
    ProfileData profileData =
        new ProfileData(target.getId(), target.getUsername(), "bio2", "image2", true);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_fail_follow_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(() -> "ROLE_ANONYMOUS")));

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void should_fail_follow_when_user_not_found() {
    when(userRepository.findByUsername("nonexist")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexist"));
  }

  @Test
  void should_unfollow_user_success() {
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    ProfileData profileData =
        new ProfileData(target.getId(), target.getUsername(), "bio2", "image2", false);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(user.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername("targetuser", user))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_fail_unfollow_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key", "anonymous", Arrays.asList(() -> "ROLE_ANONYMOUS")));

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void should_fail_unfollow_when_user_not_found() {
    when(userRepository.findByUsername("nonexist")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexist"));
  }

  @Test
  void should_fail_unfollow_when_relation_not_found() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(user.getId(), target.getId())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}

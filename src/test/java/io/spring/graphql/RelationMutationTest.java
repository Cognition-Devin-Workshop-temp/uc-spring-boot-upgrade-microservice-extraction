package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
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
    target = new User("target@test.com", "targetuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_follow_user_success() {
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    ProfileData profileData = new ProfileData(target.getId(), "targetuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");
    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_follow_throw_when_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void should_follow_throw_when_user_not_found() {
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void should_unfollow_user_success() {
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findRelation(user.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(target.getId(), "targetuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");
    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_unfollow_throw_when_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void should_unfollow_throw_when_user_not_found() {
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void should_unfollow_throw_when_no_relation_found() {
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    when(userRepository.findRelation(user.getId(), target.getId())).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}

package com.array.banking.service;

import com.array.banking.model.User;
import com.array.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "hashedpassword", "test@example.com");
        testUser.setUserId(1);
    }
    
    @Test
    void getUserByUsername_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByUsername("testuser");
        
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("test@example.com");
        
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }
    
    @Test
    void createUser_ShouldCreateUser_WhenUserDoesNotExist() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(
                new User("newuser", "hashedpassword", "new@example.com"));
        
        User result = userService.createUser("newuser", "password", "new@example.com");
        
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("hashedpassword", result.getPasswordHash());
        assertEquals("new@example.com", result.getEmail());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void createUser_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("testuser", "password", "new@example.com")
        );
        
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }
    
    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser("newuser", "password", "test@example.com")
        );
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("test@example.com");
        verifyNoMoreInteractions(userRepository);
    }
    
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        UserDetails result = userService.loadUserByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("hashedpassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER")));
        
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> 
            userService.loadUserByUsername("nonexistent")
        );
        
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    void findRandomUser_ShouldReturnUser_WhenUsersExist() {
        when(userRepository.findRandomUser()).thenReturn(Optional.of(testUser));
        
        User result = userService.findRandomUser();
        
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        verify(userRepository).findRandomUser();
    }
    
    @Test
    void findRandomUser_ShouldThrowException_WhenNoUsersExist() {
        when(userRepository.findRandomUser()).thenReturn(Optional.empty());
        
        assertThrows(IllegalStateException.class, () -> 
            userService.findRandomUser()
        );
        
        verify(userRepository).findRandomUser();
    }
}

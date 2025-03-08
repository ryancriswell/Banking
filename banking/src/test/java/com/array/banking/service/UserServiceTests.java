// package com.array.banking.service;

// import com.array.banking.model.User;
// import com.array.banking.repository.UserRepository;
// import com.array.banking.service.UserService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import java.math.BigDecimal;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class UserServiceTests {
  
//   @Mock
//   private UserRepository userRepository;
  
//   @Mock
//   private PasswordEncoder passwordEncoder;
  
//   @InjectMocks
//   private UserService userService;
  
//   private User testUser;
  
//   @BeforeEach
//   void setUp() {
//     testUser = new User("testuser", "hashedpassword", "test@example.com");
//     testUser.setUserId(1);
//     testUser.setBalance(new BigDecimal("1000.00"));
//   }
  
//   @Test
//   void getUserByUsername_ShouldReturnUser_WhenUserExists() {
//     when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    
//     Optional<User> result = userService.getUserByUsername("testuser");
    
//     assertTrue(result.isPresent());
//     assertEquals("testuser", result.get().getUsername());
//     verify(userRepository, times(1)).findByUsername("testuser");
//   }
  
//   @Test
//   void createUser_ShouldCreateUser_WhenUsernameAndEmailAreUnique() {
//     when(userRepository.existsByUsername(anyString())).thenReturn(false);
//     when(userRepository.existsByEmail(anyString())).thenReturn(false);
//     when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
//     when(userRepository.save(any(User.class))).thenReturn(testUser);
    
//     User result = userService.createUser("testuser", "password", "test@example.com");
    
//     assertNotNull(result);
//     assertEquals("testuser", result.getUsername());
//     verify(passwordEncoder, times(1)).encode("password");
//     verify(userRepository, times(1)).save(any(User.class));
//   }
  
//   @Test
//   void createUser_ShouldThrowException_WhenUsernameExists() {
//     when(userRepository.existsByUsername("testuser")).thenReturn(true);
    
//     IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//       userService.createUser("testuser", "password", "test@example.com");
//     });
    
//     assertEquals("Username already exists", exception.getMessage());
//     verify(userRepository, never()).save(any(User.class));
//   }
  
//   @Test
//   void updateUserBalance_ShouldUpdateBalance_WhenBalanceIsValid() {
//     BigDecimal newBalance = new BigDecimal("2000.00");
    
//     userService.updateUserBalance(testUser, newBalance);
    
//     assertEquals(newBalance, testUser.getBalance());
//     verify(userRepository, times(1)).save(testUser);
//   }
  
//   @Test
//   void updateUserBalance_ShouldThrowException_WhenBalanceIsNegative() {
//     BigDecimal negativeBalance = new BigDecimal("-100.00");
    
//     IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//       userService.updateUserBalance(testUser, negativeBalance);
//     });
    
//     assertEquals("Balance cannot be negative", exception.getMessage());
//     verify(userRepository, never()).save(any(User.class));
//   }
  
//   @Test
//   void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
//     when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    
//     UserDetails result = userService.loadUserByUsername("testuser");
    
//     assertNotNull(result);
//     assertEquals("testuser", result.getUsername());
//     assertEquals("hashedpassword", result.getPassword());
//     assertEquals(1, result.getAuthorities().size());
//   }
  
//   @Test
//   void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
//     when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
    
//     assertThrows(UsernameNotFoundException.class, () -> {
//       userService.loadUserByUsername("nonexistent");
//     });
//   }
// }

package com.miftah.pengaduan_masyarakat.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails mockUserDetails;

    private String testSecretKey;

    private long testJwtExpiration;

    private final String testUsername = "testuser@example.com";

    @BeforeEach
    void setUp() {

        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

        testSecretKey = Encoders.BASE64.encode(key.getEncoded());
        testJwtExpiration = TimeUnit.HOURS.toMillis(1);

        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testJwtExpiration);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void generateToken_shouldReturnValidToken() {
        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        String token = jwtService.generateToken(mockUserDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedUsername = jwtService.extractUsername(token);
        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);
        Date issuedAtDate = jwtService.extractClaim(token, Claims::getIssuedAt);

        assertEquals(testUsername, extractedUsername);
        assertNotNull(expirationDate);
        assertNotNull(issuedAtDate);
        assertTrue(expirationDate.after(issuedAtDate));

        long expectedExpirationMillis = System.currentTimeMillis() + testJwtExpiration;
        assertTrue(Math.abs(expirationDate.getTime() - expectedExpirationMillis) < 5000, "Expiration time mismatch");
    }

    @Test
    @DisplayName("Should generate a token with extra claims")
    void generateToken_withExtraClaims_shouldContainClaims() {

        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", "user-123");
        extraClaims.put("role", "ADMIN");

        String token = jwtService.generateToken(extraClaims, mockUserDetails);

        assertNotNull(token);

        String extractedUserId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
        String extractedRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("user-123", extractedUserId);
        assertEquals("ADMIN", extractedRole);
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    @DisplayName("Should extract username (subject) from token")
    void extractUsername_shouldReturnCorrectUsername() {

        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        String token = jwtService.generateToken(mockUserDetails);
        String username = jwtService.extractUsername(token);
        assertEquals(testUsername, username);
    }

    @Test
    @DisplayName("Should extract specific claim from token")
    void extractClaim_shouldReturnCorrectClaim() {

        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        String token = jwtService.generateToken(mockUserDetails);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        assertNotNull(expiration);
        assertEquals(testUsername, subject);
    }

    @Test
    @DisplayName("Should validate a correct and unexpired token")
    void isTokenValid_whenTokenIsValid_shouldReturnTrue() {

        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        String token = jwtService.generateToken(mockUserDetails);
        boolean isValid = jwtService.isTokenValid(token, mockUserDetails);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate a token with incorrect username")
    void isTokenValid_whenUsernameMismatch_shouldReturnFalse() {

        when(mockUserDetails.getUsername()).thenReturn(testUsername);
        String token = jwtService.generateToken(mockUserDetails);

        UserDetails differentUserDetails = org.mockito.Mockito.mock(UserDetails.class);
        when(differentUserDetails.getUsername()).thenReturn("anotheruser@example.com");

        boolean isValid = jwtService.isTokenValid(token, differentUserDetails);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException when parsing an expired token")
    void extractAllClaims_whenTokenIsExpired_shouldThrowExpiredJwtException() throws InterruptedException {
        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        long shortExpiration = 50;
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", shortExpiration);

        String token = jwtService.generateToken(mockUserDetails);

        Thread.sleep(shortExpiration + 20);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractUsername(token);
        }, "Parsing an expired token should throw ExpiredJwtException");

        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testJwtExpiration);
    }

    @Test
    @DisplayName("Should correctly identify an unexpired token")
    void isTokenExpired_whenTokenIsNotExpired_shouldReturnFalse() {

        when(mockUserDetails.getUsername()).thenReturn(testUsername);

        String token = jwtService.generateToken(mockUserDetails);
        boolean isValid = jwtService.isTokenValid(token, mockUserDetails);
        assertTrue(isValid, "A freshly generated token should be valid (and thus not expired)");
    }

    @Test
    @DisplayName("Should return the configured expiration time")
    void getExpirationTime_shouldReturnCorrectValue() {

        long expirationTime = jwtService.getExpirationTime();
        assertEquals(testJwtExpiration, expirationTime);
    }
}

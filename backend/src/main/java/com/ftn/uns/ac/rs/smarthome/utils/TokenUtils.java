package com.ftn.uns.ac.rs.smarthome.utils;

import com.ftn.uns.ac.rs.smarthome.models.Role;
import com.ftn.uns.ac.rs.smarthome.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

//Tạo, giải mã, xác thực và trích xuất thông tin từ JWT Token
@Component
public class TokenUtils {
	@Value("smart-home")
	private String APP_NAME;

	@Value("${jwt.secret}")
	public String SECRET;

	@Value("18000000")
	private int EXPIRES_IN;

	@Value("Authorization")
	private String AUTH_HEADER;

	private static final String AUDIENCE_WEB = "web";

	private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
	

// Tạo JWT Token
	public String generateToken(String username, Role role) {
		return Jwts.builder()
				.setIssuer(APP_NAME)
				.setSubject(username)
				.setAudience(generateAudience())
				.setIssuedAt(new Date())
				.setExpiration(generateExpirationDate())
				.claim("role", role.getName())
				.signWith(SIGNATURE_ALGORITHM, SECRET).compact();   //xử lý Signature
	}
	

	private String generateAudience() {
		return AUDIENCE_WEB;
	}


	private Date generateExpirationDate() {
		return new Date(new Date().getTime() + EXPIRES_IN);
	}


//Trích token từ header
	public String getToken(HttpServletRequest request) {
		String authHeader = getAuthHeaderFromHeader(request);
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7); // Bỏ đi "Bearer " để lấy phần token
		}
		return null;
	}
	
// giai ma và lấy thông tin tên ng dùng
	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			username = claims.getSubject();    //Đọc thông tin từ payload
		} catch (ExpiredJwtException ex) {
			throw ex;
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	// giai ma va doc token lấy thời hạn tạo
	public Date getIssuedAtDateFromToken(String token) {
		Date issueAt;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			issueAt = claims.getIssuedAt();    //Đọc thông tin từ payload
		} catch (ExpiredJwtException ex) {
			throw ex;
		} catch (Exception e) {
			issueAt = null;
		}
		return issueAt;
	}

	// giai ma va doc token lấy thông tin loại thiết bị
	public String getAudienceFromToken(String token) {
		String audience;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			audience = claims.getAudience();   //Đọc thông tin từ payload
		} catch (ExpiredJwtException ex) {
			throw ex;
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}

// thoi gian hết hạn token
	public Date getExpirationDateFromToken(String token) {
		Date expiration;
		try {
			final Claims claims = this.getAllClaimsFromToken(token);
			expiration = claims.getExpiration();   //Đọc thông tin từ payload
		} catch (ExpiredJwtException ex) {
			throw ex;
		} catch (Exception e) {
			expiration = null;
		}
		
		return expiration;
	}
	
//Giải mã token bằng SECRET đã ký:
	private Claims getAllClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser()
					.setSigningKey(SECRET)
					.parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException ex) {
			throw ex;
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}
	

// xac thuc token
	public Boolean validateToken(String token, UserDetails userDetails) {
		User user = (User) userDetails;
		final String username = getUsernameFromToken(token);   //lấy user tù token
		final Date created = getIssuedAtDateFromToken(token);// // Lấy thời điểm token được tạo (iat)

		return (username != null && username.equals(userDetails.getUsername()));

	}


	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}
	

	public int getExpiredIn() {
		return EXPIRES_IN;
	}

//Đọc giá trị header "Authorization"
	public String getAuthHeaderFromHeader(HttpServletRequest request) {
		return request.getHeader(AUTH_HEADER);
	}
	
}
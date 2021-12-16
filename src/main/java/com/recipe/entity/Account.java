package com.recipe.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.recipe.dto.AccountUpdateDto;

@Entity
@Table(name="account", uniqueConstraints={@UniqueConstraint(columnNames={"loginid", "type"})})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
//@DynamicInsert
public class Account implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@Column(name="loginid", nullable=false)
	private String loginid;
	
	// 소셜 - kakao, google, naver
	@Column(name="type", nullable=false)
	private String type;
	
	@Column(name="auth")
//	@ColumnDefault("ROLE_USER")
	private String auth;
	
	@Column(name="email")
	private String email;
	
	@Column(name="nickname", nullable=false)
	private String nickname;
	
	@Column(name="imageUrl")
	private String imageUrl;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="account", orphanRemoval=true)
    private List<LikeRecipe> likeRecipe;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="account", orphanRemoval=true)
    private List<Post> posts;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="account", orphanRemoval=true)
    private List<Comment> comments;
	
	@Column(name="createAt")
	@CreationTimestamp
	private LocalDateTime createAt;
	
	@Column(name="updateAt")
	@UpdateTimestamp
	private LocalDateTime updateAt;
	
	@Builder
	public Account(String loginid, String type, String auth, String nickname, String imageUrl) {
		this.loginid = loginid;
		this.type = type;
		this.auth = auth;
		this.nickname = nickname;
		this.imageUrl = imageUrl;
	}
	
	@Builder
	public Account(String loginid, String type, String auth, String email, String nickname, String imageUrl) {
		this.loginid = loginid;
		this.type = type;
		this.auth = auth;
		this.email = email;
		this.nickname = nickname;
		this.imageUrl = imageUrl;
	}
	
	public void update(AccountUpdateDto infoDto) {
		this.nickname = infoDto.getNickname();
		this.imageUrl = infoDto.getImageUrl();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> roles = new HashSet<>();
        for (String role : auth.split(",")) {
            roles.add(new SimpleGrantedAuthority(role));
        }
        return roles;
	}

	// 사용자의 id를 반환 (unique한 값)
    @Override
    public String getUsername() {
        return loginid;
    }

    // 사용자의 password를 반환
    @Override
    public String getPassword() {
        return null;
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        return true; // true -> 잠금되지 않았음
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }

}

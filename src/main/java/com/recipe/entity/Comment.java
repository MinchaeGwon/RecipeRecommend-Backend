package com.recipe.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(name="recipe_comment")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(name="content", nullable=false)
	private String content;
	
	@ManyToOne
	@JoinColumn(name="post_id")
	private Post post;
	
	@ManyToOne
	@JoinColumn(name="account_id")
	private Account account;

	@OneToMany(cascade = CascadeType.ALL, mappedBy="comment", orphanRemoval=true)
	private List<RecommendComment> recommend;
	
	@Column(name="createAt")
	@CreationTimestamp
	private LocalDateTime createAt;
	
	@Column(name="updateAt")
	@UpdateTimestamp
	private LocalDateTime updateAt;
	
	@Builder
	public Comment(String content, Post post, Account account) {
		this.content = content;
		this.post = post;
		this.account = account;
	}
	
	public void update(String content) {
		this.content = content;
	}
	
}

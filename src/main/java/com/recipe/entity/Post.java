package com.recipe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.recipe.dto.PostUpdateDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="recipe_post")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@Column(name="title", nullable=false)
	private String title;
	
	@Column(name="content", nullable=false)
	private String content;
	
	@Column(name="imageUrl")
	private String imageUrl;
	
	@Column(name="price")
	private int price;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="post", orphanRemoval=true)
	private List<Comment> comment;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="post", orphanRemoval=true)
	private List<RecommendPost> recommend;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="post", orphanRemoval=true)
	private List<LikeRecipe> like;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="post", orphanRemoval=true)
	private List<Tag> tags = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="post", orphanRemoval=true)
	private List<View> views = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name="account_id")
	private Account account;
	
	@ManyToOne
	@JoinColumn(name="brand_id")
	private Brand brand;
	
	@ManyToOne
	@JoinColumn(name="category_id")
	private Category category;
	
	@Column(name="createAt")
	@CreationTimestamp
	private LocalDateTime createAt;
	
	@Column(name="updateAt")
	@UpdateTimestamp
	private LocalDateTime updateAt;
	
	@Builder
	public Post(String title, String content, String imageUrl, int price, Account account, Category category, Brand brand) {
		this.title = title;
		this.content = content;
		this.imageUrl = imageUrl;
		this.price = price;
		this.account = account;
		this.category = category;
		this.brand = brand;
	}
	
	public void update(PostUpdateDto info) {
		this.title = info.getTitle();
		this.content = info.getContent();
		this.imageUrl = info.getImageUrl();
		this.price = info.getPrice();
	}
}

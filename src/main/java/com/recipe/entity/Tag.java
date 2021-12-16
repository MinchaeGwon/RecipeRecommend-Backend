package com.recipe.entity;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name="recipe_tag")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@Column(name="tagname")
	private String tagname;
	
	@ManyToOne
	@JoinColumn(name="post_id")
	private Post post;
	
	@ManyToOne
	@JoinColumn(name="account_id")
	private Account account;
	
	@Builder
	public Tag(String tagname, Post post, Account account) {
		this.tagname = tagname;
		this.post = post;
		this.account = account;
	}
}

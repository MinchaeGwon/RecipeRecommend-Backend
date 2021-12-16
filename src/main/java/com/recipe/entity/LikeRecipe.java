package com.recipe.entity;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name="like_recipe", uniqueConstraints={@UniqueConstraint(columnNames={"post_id","account_id"})})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeRecipe {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="post_id")
	private Post post;
	
	@ManyToOne
	@JoinColumn(name="account_id")
	private Account account;
	
	@Builder
	public LikeRecipe(Post post, Account account) {
		this.post = post;
		this.account = account;
	}
}

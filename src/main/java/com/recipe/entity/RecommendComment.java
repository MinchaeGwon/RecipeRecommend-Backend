package com.recipe.entity;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name="recommend_comment", uniqueConstraints={@UniqueConstraint(columnNames={"comment_id","account_id"})})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="comment_id")
	private Comment comment;
	
	@ManyToOne
	@JoinColumn(name="account_id")
	private Account account;
	
	@Builder
	public RecommendComment(Comment comment, Account account) {
		this.comment = comment;
		this.account = account;
	}

}

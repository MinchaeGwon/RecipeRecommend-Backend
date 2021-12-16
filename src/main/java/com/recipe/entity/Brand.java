package com.recipe.entity;

import java.util.List;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name="brand")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Brand {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(name="brandname")
	private String brandname;
	
	@ManyToOne
	@JoinColumn(name="category_id")
	private Category category;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="brand", orphanRemoval=true)
	private List<Post> post;
	
	@Builder
	public Brand(String brandname, Category category) {
		this.brandname = brandname;
		this.category = category;
	}
}

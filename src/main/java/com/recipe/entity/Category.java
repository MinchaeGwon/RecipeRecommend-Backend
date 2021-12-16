package com.recipe.entity;

import java.util.List;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name="category")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@Column(name="categoryname")
	private String categoryname;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="category", orphanRemoval=true)
	private List<Brand> brand;
}

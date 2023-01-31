package sg.edu.nus.iss.AD_Locum_Doctors.model;

import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Clinic {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String address;

	private String postalCode;

	private String HCICode;

	private String contact;

	@ManyToOne
	private Organization organization;

	@OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
	private List<JobPost> jobPosts;
}

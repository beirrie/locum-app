package sg.edu.nus.iss.AD_Locum_Doctors.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Organization {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String UEN;

	@OneToMany(mappedBy = "organization")
	private List<User> users;

	@OneToMany(mappedBy = "organization")
	private List<Clinic> clinics = new ArrayList<>();
}
package sg.edu.nus.iss.AD_Locum_Doctors.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sg.edu.nus.iss.AD_Locum_Doctors.model.FirebaseDeviceToken;

@Repository
public interface FirebaseRepository extends JpaRepository<FirebaseDeviceToken,Integer> {

	// FirebaseDeviceToken findByLoginUserName(String loginUserName);
	@Query("select f from FirebaseDeviceToken f where f.user.username = :loginUserName")
	public FirebaseDeviceToken findDeviceTokenByUserName(@Param("loginUserName") String loginUserName);
}

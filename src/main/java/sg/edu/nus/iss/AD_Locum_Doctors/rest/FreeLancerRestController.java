package sg.edu.nus.iss.AD_Locum_Doctors.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.AD_Locum_Doctors.model.FreeLancerDTO;
import sg.edu.nus.iss.AD_Locum_Doctors.service.EmailService;
import sg.edu.nus.iss.AD_Locum_Doctors.service.FirebaseServiceImpl;
import sg.edu.nus.iss.AD_Locum_Doctors.service.PasswordResetCheckerService;
import sg.edu.nus.iss.AD_Locum_Doctors.service.UserService;

@RestController
@RequestMapping("api/freelancer")
public class FreeLancerRestController {

	@Autowired
	UserService userService;

	@Autowired
	FirebaseServiceImpl firebaseService;

	@Autowired
	private PasswordResetCheckerService passwordResetCheckerService;

	@Autowired
	private EmailService emailService;

	@PostMapping("/login")
	public ResponseEntity<FreeLancerDTO> loginNewFreeLancer(@RequestBody FreeLancerDTO freeLancerDTO) {
		try {
			// updates all dto fields with registered user data
			FreeLancerDTO existingFreeLancer = userService.loginFreeLancerAndUpdateToken(freeLancerDTO);
			if (existingFreeLancer == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
			}
			return new ResponseEntity<>(existingFreeLancer, HttpStatus.OK); // 200
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/register")
	public ResponseEntity<FreeLancerDTO> registerNewFreeLancer(@RequestBody FreeLancerDTO freeLancerDTO) {
		try {
			FreeLancerDTO newFreeLancerDTO = userService.createFreeLancer(freeLancerDTO);
			if (newFreeLancerDTO.getErrorsFieldString().isEmpty()) {
				return new ResponseEntity<>(newFreeLancerDTO, HttpStatus.CREATED);
			}
			// return errorString to client indicate input
			// fields(username,Email,medicalLicenseNo) are non-unique
			else {
				return new ResponseEntity<>(newFreeLancerDTO, HttpStatus.NOT_ACCEPTABLE);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/update")
	public ResponseEntity<FreeLancerDTO> updateNewFreeLancer(@RequestBody FreeLancerDTO freeLancerDTO) {
		try {
			FreeLancerDTO updatedFL = userService.updateFreeLancer(freeLancerDTO);
			if (updatedFL != null && updatedFL.getErrorsFieldString().isEmpty()) {
				return new ResponseEntity<>(updatedFL, HttpStatus.OK);
			}
			return new ResponseEntity<>(updatedFL, HttpStatus.NOT_ACCEPTABLE);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/logout")
	public ResponseEntity<String> removeToken(@RequestParam String username) {
		try {
			userService.logoutFreeLancer(username);
			return new ResponseEntity<>("Logout success", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Logout failed due to server-side error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

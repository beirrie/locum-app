package sg.edu.nus.iss.AD_Locum_Doctors.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sg.edu.nus.iss.AD_Locum_Doctors.model.FirebaseDeviceToken;
import sg.edu.nus.iss.AD_Locum_Doctors.model.FreeLancerDTO;
import sg.edu.nus.iss.AD_Locum_Doctors.model.Role;
import sg.edu.nus.iss.AD_Locum_Doctors.model.User;
import sg.edu.nus.iss.AD_Locum_Doctors.repository.FirebaseRepository;
import sg.edu.nus.iss.AD_Locum_Doctors.repository.RoleRepository;
import sg.edu.nus.iss.AD_Locum_Doctors.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	RoleRepository roleRepo;

	@Autowired
	FirebaseRepository firebaseRepo;

	@Override
	public void saveUser(User user) {
		userRepo.saveAndFlush(user);
	}

	@Override
	public void editUser(User user) {
		var dbUser = findById(user.getId());
		dbUser.setName(user.getName());
		dbUser.setEmail(user.getEmail());
		dbUser.setContact(user.getContact());
		dbUser.setMedicalLicenseNo(user.getMedicalLicenseNo());
		dbUser.setRole(user.getRole());
		userRepo.saveAndFlush(dbUser);
	}

	@Override
	public List<User> findAll() {
		return userRepo.findAll();
	}

	@Override
	public User findById(Long id) {
		return userRepo.findById(id).get();
	}

	@Override
	public User authenticate(String username, String pwd) {
		return userRepo.findAll().stream()
				.filter(u -> u.getPassword().equals(pwd) && !u.getRole().getName().equals("Locum_Doctor")
						&& u.getUsername().equalsIgnoreCase(username) && u.getActive())
				.findFirst().orElse(null);
	}

	@Override
	public User authenticateDoc(String username, String pwd) {
		return userRepo.findAll().stream()
				.filter(u -> u.getPassword().equals(pwd)
						&& u.getUsername().equalsIgnoreCase(username) && u.getActive())
				.findFirst().orElse(null);
	}

	@Override
	public void deleteUser(User user) {
		userRepo.delete(user);
	}

	@Override
	public User deactivateUser(User user) {
		user.setActive(false);
		return userRepo.saveAndFlush(user);
	}

	@Override
	public User reactivateUser(User user) {
		user.setActive(true);
		return userRepo.saveAndFlush(user);
	}

	@Override
	public FreeLancerDTO createFreeLancer(FreeLancerDTO freeLancerDTO) {

		// Check against all Registered users. Username,Email,medicalLicenseNo must be
		// unique
		String errorsFieldString = "";
		List<User> allRegisteredUsersList = userRepo.findAll();
		if (!allRegisteredUsersList.isEmpty()) {
			for (User currentUser : allRegisteredUsersList) {
				// Case insensitive comparison with DB Usernames
				if (currentUser.getUsername() != null
						&& currentUser.getUsername().equalsIgnoreCase(freeLancerDTO.getUsername())) {
					errorsFieldString += "Username";
				}
				if (currentUser.getEmail() != null
						&& currentUser.getEmail().equalsIgnoreCase(freeLancerDTO.getEmail())) {
					errorsFieldString += "Email";
				}
				if (currentUser.getMedicalLicenseNo() != null
						&& currentUser.getMedicalLicenseNo().equalsIgnoreCase(freeLancerDTO.getMedicalLicenseNo())) {
					errorsFieldString += "Medical";
				}
			}
		}

		// Username/email/medicalLicense is not unique, already present in DB record
		if (!errorsFieldString.isEmpty()) {
			freeLancerDTO.setErrorsFieldString(errorsFieldString);
			return freeLancerDTO;
		}

		// proceed to register new FreeLancerUser
		try {
			User newFreeLancerUser = new User();
			newFreeLancerUser.setName(freeLancerDTO.getName());
			newFreeLancerUser.setUsername(freeLancerDTO.getUsername());
			newFreeLancerUser.setPassword(freeLancerDTO.getPassword());
			newFreeLancerUser.setContact(freeLancerDTO.getContact());
			newFreeLancerUser.setEmail(freeLancerDTO.getEmail());
			newFreeLancerUser.setMedicalLicenseNo(freeLancerDTO.getMedicalLicenseNo());
			// set role
			Role locumDoctorRole = roleRepo.findRole("Locum_Doctor");
			newFreeLancerUser.setRole(locumDoctorRole);
			// set firebasetoken
			FirebaseDeviceToken newFirebaseForUser = new FirebaseDeviceToken(freeLancerDTO.getDeviceToken(), true,
					freeLancerDTO.getUsername());
			firebaseRepo.saveAndFlush(newFirebaseForUser);
			newFreeLancerUser.setFirebaseDeviceToken(newFirebaseForUser);
			userRepo.saveAndFlush(newFreeLancerUser);

			freeLancerDTO.setId(newFreeLancerUser.getId().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return freeLancerDTO;
	}

	@Override
	public FreeLancerDTO loginFreeLancerAndUpdateToken(FreeLancerDTO freeLancerDTO) {

		User existingUser = authenticateDoc(freeLancerDTO.getUsername(), freeLancerDTO.getPassword());

		// Found Registered User
		if (existingUser != null && existingUser.getRole().getName().equals("Locum_Doctor")) {
			freeLancerDTO.setId(existingUser.getId().toString()); // tag id
			freeLancerDTO.setName(existingUser.getName());
			freeLancerDTO.setUsername(existingUser.getUsername());
			freeLancerDTO.setContact(existingUser.getContact());
			freeLancerDTO.setEmail(existingUser.getEmail());
			freeLancerDTO.setMedicalLicenseNo(existingUser.getMedicalLicenseNo());

			System.out.println("username: " + freeLancerDTO.getUsername());
			System.out.println("device Token: " + freeLancerDTO.getDeviceToken());

			// Update Device Token
			FirebaseDeviceToken existingUserFireBase = existingUser.getFirebaseDeviceToken();

			if (existingUserFireBase != null) {
				existingUserFireBase.setToken(freeLancerDTO.getDeviceToken());
				existingUserFireBase.setIsLoggedIntoMobileApp(true);
			}

			existingUser.setFirebaseDeviceToken(existingUserFireBase);
			userRepo.saveAndFlush(existingUser);

			return freeLancerDTO;
		}
		return null;
	}

	@Override
	public FreeLancerDTO updateFreeLancer(FreeLancerDTO freeLancerDTO) {
		User existingUser = userRepo.findById(Long.valueOf(freeLancerDTO.getId())).orElse(null);
		if (existingUser != null && freeLancerDTO != null) {
			List<User> checkAgainstUsers = userRepo.findAll().stream()
					.filter(u -> u.getId() != existingUser.getId())
					.toList();

			if (!freeLancerDTO.getContact().equalsIgnoreCase(existingUser.getContact())) {
				existingUser.setContact(freeLancerDTO.getContact());
			}
			if (!freeLancerDTO.getName().equalsIgnoreCase(existingUser.getName())) {
				existingUser.setName(freeLancerDTO.getName());
			}
			if (!freeLancerDTO.getPassword().equals(existingUser.getPassword())) {
				existingUser.setPassword(freeLancerDTO.getPassword());
			}

			// check if new email and medicalLicenseNo exists in DB record
			if (!checkAgainstUsers.isEmpty()) {
				String errString = "";
				if (!freeLancerDTO.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
					String result = checkIfFieldIsUnique(checkAgainstUsers, "Email", freeLancerDTO.getEmail());
					if (!result.contains("Email")) {
						existingUser.setEmail(freeLancerDTO.getEmail());
					} else {
						errString += result;
					}
				}
				if (!freeLancerDTO.getMedicalLicenseNo().equalsIgnoreCase(existingUser.getMedicalLicenseNo())) {
					String result = checkIfFieldIsUnique(checkAgainstUsers, "Medical",
							freeLancerDTO.getMedicalLicenseNo());
					if (!result.contains("Medical")) {
						existingUser.setMedicalLicenseNo(freeLancerDTO.getMedicalLicenseNo());
					} else {
						errString += result;
					}
				}
				userRepo.saveAndFlush(existingUser);
				freeLancerDTO.setErrorsFieldString(errString);
				return freeLancerDTO;
			}
		}
		return null;
	}

	@Override
	public void logoutFreeLancer(String username) {
		User freeLancerToLogOut = userRepo.findByUsername(username);
		if (freeLancerToLogOut != null && freeLancerToLogOut.getFirebaseDeviceToken() != null) {
			freeLancerToLogOut.getFirebaseDeviceToken().setIsLoggedIntoMobileApp(false);
			userRepo.saveAndFlush(freeLancerToLogOut);
		}
	}

	@Override
	public String checkIfFieldIsUnique(List<User> checkAgainstUsers, String fieldName, String fieldValue) {
		String errStr = "err";
		try {
			for (User user : checkAgainstUsers) {
				if (fieldName.equals("Email")) {
					if (user.getEmail().equalsIgnoreCase(fieldValue)) {
						errStr += fieldName;
						return errStr;
					}
				}
				if (fieldName.equals("Medical")) {
					if (user.getMedicalLicenseNo() != null && user.getMedicalLicenseNo().equalsIgnoreCase(fieldValue)) {
						errStr += fieldName;
						return errStr;
					}
				}
			}
			return errStr;
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		return errStr;
	}

	@Override
	public List<User> findByOrganizationId(Long id) {
		return userRepo.findByOrganizationId(id);
	}

}

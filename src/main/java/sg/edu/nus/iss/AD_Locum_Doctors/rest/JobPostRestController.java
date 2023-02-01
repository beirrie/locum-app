package sg.edu.nus.iss.AD_Locum_Doctors.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sg.edu.nus.iss.AD_Locum_Doctors.model.*;
import sg.edu.nus.iss.AD_Locum_Doctors.service.JobPostService;
import sg.edu.nus.iss.AD_Locum_Doctors.service.UserService;

@RestController
@RequestMapping("api/jobs")
public class JobPostRestController {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    JobPostService jobPostService;

    @Autowired
    UserService userService;

    @GetMapping("/allopen")
    public ResponseEntity<List<JobPostApiDTO>> findAllOpen() {
        try {
            List<JobPost> jobPostList = jobPostService.findAllOpen();
            List<JobPostApiDTO> jobPostDTOList = new ArrayList<>();
            for (JobPost jobPost : jobPostList) {
                JobPostApiDTO jobPostDTO = setJobPostDTO(jobPost);
                jobPostDTOList.add(jobPostDTO);
            }
            if (jobPostList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(jobPostDTOList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/job")
    public ResponseEntity<JobPostApiDTO> findById(@RequestParam String id) {
        try {
            JobPost jobPost = jobPostService.findJobPostById(id);
            if (jobPost != null) {
                JobPostApiDTO jobPostDTO = setJobPostDTO(jobPost);
                return new ResponseEntity<>(jobPostDTO, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/job")
    public ResponseEntity<JobPostApiDTO> setJobStatus(@RequestParam String id, @RequestParam String status,
            @RequestParam String userId) {
        try {
            JobPost jobPost = jobPostService.findJobPostById(id);
            if (jobPost != null) {
                if (Objects.equals(status, "apply")) {
                    jobPostService.setStatus(jobPost, JobStatus.PENDING_CONFIRMATION_BY_CLINIC, userId, null);
                } else if (Objects.equals(status, "cancel")) {
                    User user = userService.findById(Long.valueOf(userId));
                    JobAdditionalRemarks additionalRemarks = new JobAdditionalRemarks();
                    additionalRemarks.setCategory(RemarksCategory.CANCELLATION);
                    additionalRemarks.setRemarks("Application Cancelled");
                    additionalRemarks.setDate(LocalDate.now());
                    additionalRemarks.setJobPost(jobPost);
                    additionalRemarks.setUser(user);
                    jobPostService.setStatus(jobPost, JobStatus.OPEN, userId, additionalRemarks);
                }
                JobPostApiDTO jobPostDTO = setJobPostDTO(jobPost);
                return new ResponseEntity<>(jobPostDTO, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<JobPostApiDTO>> findJobHistory(@RequestParam String id) {
        try {

            List<JobPost> jobPostList = jobPostService.findJobHistory(id);
            List<JobPostApiDTO> jobPostDTOList = new ArrayList<>();
            for (JobPost jobPost : jobPostList) {
                JobPostApiDTO jobPostDTO = setJobPostDTO(jobPost);
                jobPostDTOList.add(jobPostDTO);
            }
            if (jobPostList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(jobPostDTOList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private JobPostApiDTO setJobPostDTO(JobPost jobPost) {
        JobPostApiDTO jobPostDTO = new JobPostApiDTO();
        jobPostDTO.setId(jobPost.getId());
        jobPostDTO.setDescription(jobPost.getDescription());
        jobPostDTO.setStartDateTime(jobPost.getStartDateTime().toString());
        jobPostDTO.setEndDateTime(jobPost.getEndDateTime().toString());
        jobPostDTO.setClinic(jobPost.getClinic());
        jobPostDTO.setStatus(jobPost.getStatus());
        jobPostDTO.setTotalRate(jobPost.getTotalRate());
        jobPostDTO.setRatePerHour(jobPost.getRatePerHour());
        jobPostDTO.setClinicUser(jobPost.getClinicUser());
        jobPostDTO.setClinic(jobPost.getClinic());
        jobPostDTO.setFreelancer(jobPost.getFreelancer());

        return jobPostDTO;
    }
}

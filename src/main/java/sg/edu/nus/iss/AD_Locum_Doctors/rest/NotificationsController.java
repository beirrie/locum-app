package sg.edu.nus.iss.AD_Locum_Doctors.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.AD_Locum_Doctors.model.Notification;
import sg.edu.nus.iss.AD_Locum_Doctors.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/notifications")
public class NotificationsController {

    @Autowired
    NotificationService notificationService;

    @GetMapping("/get")
    public ResponseEntity<List<Notification>> getByUserId(@RequestParam Long userId) {
        try {
            List<Notification> notificationList = notificationService.getNotifications(userId);
            return new ResponseEntity<>(notificationList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkNotification(@RequestParam Long userId) {
        try {
            List<Notification> notificationList = notificationService.getNotifications(userId);
            for (Notification n : notificationList) {
                if (!n.isNotificationRead()) {
                    return new ResponseEntity<>(false, HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/setread")
    public ResponseEntity<List<Notification>> setAsRead(@RequestParam Long notificationId) {
        try {
            Optional<Notification> notification_optional = notificationService.findById(notificationId);
            List<Notification> notificationList = new ArrayList<>();
            if (notification_optional.isPresent()) {
                Notification notification = notification_optional.get();
                notification.setNotificationRead(true);
                notificationList.add(notification);
                notificationService.saveNotification(notification);
            }
            return new ResponseEntity<>(notificationList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


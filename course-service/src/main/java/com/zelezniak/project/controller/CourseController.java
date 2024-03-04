package com.zelezniak.project.controller;


import com.zelezniak.project.dto.PaymentInfo;
import com.zelezniak.project.entity.Course;
import com.zelezniak.project.entity.CourseAuthor;
import com.zelezniak.project.entity.Order;
import com.zelezniak.project.entity.Student;
import com.zelezniak.project.exception.CourseException;
import com.zelezniak.project.exception.ErrorInfo;
import com.zelezniak.project.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final AuthorService authorService;
    private final CheckoutService checkoutService;
    private final StudentService studentService;

    @GetMapping("/courses")
    public ModelAndView availableCourses(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("courses");
        List<Course> courses = courseService.getAllAvailableCourses(principal.getName());
        modelAndView.addObject("courses", courses);
        return modelAndView;
    }

    @GetMapping("/courses/payment/info")
    public ModelAndView prepareOrder(@RequestParam Long courseId, Principal principal) {
        ModelAndView modelAndView = new ModelAndView("payment-information");
        Course courseToBuy = courseService.findById(courseId);
        PaymentInfo paymentInfo = checkoutService.createPaymentInfo(courseToBuy, principal.getName());
        modelAndView.addObject("paymentInfo", paymentInfo);
        return modelAndView;
    }

    @GetMapping("/add/courses/form")
    public ModelAndView addCourseForm(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("add-courses-form");
        String email = principal.getName();
        CourseAuthor authorFromDb = authorService.findByEmail(email);
        modelAndView.addObject("authorId", authorFromDb.getAuthorId());
        modelAndView.addObject("course", new Course());
        return modelAndView;
    }

    @PostMapping("/save/courses")
    public ModelAndView saveCourse(@ModelAttribute("course") @Valid Course course, @RequestParam Long authorId, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("add-courses-form");
        ModelAndView errors = FormValidationManager.getErrors(bindingResult, modelAndView);
        if (errors != null) {return errors;}
        else {
            try {
                courseService.addCourse(course, authorId);
                modelAndView.addObject("info", "New course has been created!");
                return modelAndView;
            } catch (CourseException ex) {
                return modelAndView.addObject(
                        "errorInfo", new ErrorInfo(ex.getCourseError().getMessage()));}
        }
    }

    @GetMapping("/courses/details")
    public ModelAndView courseDetails(@RequestParam Long courseId) {
        ModelAndView modelAndView = new ModelAndView("course-details");
        Course course = courseService.findById(courseId);
        int participantsNumber = course.countTotalParticipants();
        modelAndView.addObject("course", course);
        modelAndView.addObject("participants", participantsNumber);
        return modelAndView;
    }

    @GetMapping("/author/courses")
    public ModelAndView coursesCreatedByAuthor(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("courses-created-by-author");
        String authorEmail = principal.getName();
        Set<Course> createdByAuthor = authorService.findByEmail(authorEmail).getCreatedByAuthor();
        modelAndView.addObject("courses", createdByAuthor);
        return modelAndView;
    }

    @GetMapping("/update/courses")
    public ModelAndView courseUpdateForm(@RequestParam Long courseId) {
        ModelAndView modelAndView = new ModelAndView("update-course");
        Course course = courseService.findById(courseId);
        modelAndView.addObject("course", course);
        return modelAndView;
    }

    @PostMapping("/update/courses/")
    public ModelAndView updateCourse(@RequestParam Long courseId, @ModelAttribute("course") @Valid Course course, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("update-course");
        ModelAndView errors = FormValidationManager.getErrors(bindingResult, modelAndView);
        if (errors != null) {return errors;}
        else {
            try {
                courseService.updateCourse(courseId, course);
                modelAndView.addObject("info", "course updated successfully");
            } catch (CourseException var7) {
                modelAndView.addObject("errorInfo", new ErrorInfo(var7.getCourseError().getMessage()));
            }return modelAndView;
        }
    }

    @GetMapping("/purchased/courses")
    public ModelAndView coursesBoughtByUser(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("user-courses");
        String email = principal.getName();
        CourseAuthor author = authorService.findByEmail(email);
        Student student = studentService.findByEmail(email);
        Set<Course> userCourses = author != null ? author.getBoughtCourses() : student.getBoughtCourses();
        modelAndView.addObject("courses", userCourses);
        return modelAndView;
    }

    @GetMapping({"/user/orders"})
    public ModelAndView userOrders(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("user-orders");
        String email = principal.getName();
        CourseAuthor author = authorService.findByEmail(email);
        Student student = studentService.findByEmail(email);
        Set<Order> orders = author != null ? author.getAuthorOrders() : student.getStudentOrders();
        double totalPrice = OrderService.totalOrdersPrice(orders);
        modelAndView.addObject("totalPrice", totalPrice);
        modelAndView.addObject("orders", orders);
        return modelAndView;
    }
}
package com.zelezniak.project.order;

import com.zelezniak.project.author.AuthorService;
import com.zelezniak.project.course.Course;
import com.zelezniak.project.author.CourseAuthor;
import com.zelezniak.project.student.Student;
import com.zelezniak.project.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
final class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final StudentService studentService;
    private final AuthorService authorService;

    public Order createOrder(Course course, CourseAuthor author) {
        Order order = Order
                .OrderBuilder
                .buildOrder(course,author);
        orderRepository.save(order);
        return order;
    }

    @Override
    public Order createOrder(Course course, Student student) {
        Order order = Order
                .OrderBuilder
                .buildOrder(course,student);
        orderRepository.save(order);
        return order;
    }

    @Override
    public Set<Order> getOrdersForUser(Principal principal) {
        String email = principal.getName();
        CourseAuthor author = authorService.findByEmail(email);
        Student student = studentService.findByEmail(email);
        return author != null ? author.getAuthorOrders() : student.getStudentOrders();
    }
}

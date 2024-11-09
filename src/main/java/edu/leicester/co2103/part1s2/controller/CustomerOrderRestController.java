package edu.leicester.co2103.part1s2.controller;

import edu.leicester.co2103.part1s2.domain.Author;
import edu.leicester.co2103.part1s2.domain.Book;
import edu.leicester.co2103.part1s2.domain.CustomerOrder;
import edu.leicester.co2103.part1s2.domain.ErrorInfo;
import edu.leicester.co2103.part1s2.repo.BookRepository;
import edu.leicester.co2103.part1s2.repo.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CustomerOrderRestController {
    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/orders")
    private ResponseEntity<?> getOrders() {
        List<CustomerOrder> orders = (List<CustomerOrder>) customerOrderRepository.findAll();

        return new ResponseEntity<>(
                orders,
                orders.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }

    @GetMapping("/orders/{id}")
    private ResponseEntity<?> getOrder(@PathVariable long id) {
        Optional<CustomerOrder> customerOrder = customerOrderRepository.findById(id);

        return customerOrder.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity(new ErrorInfo("Order with id %s not found".formatted(id)), HttpStatus.NOT_FOUND));
    }

    @PostMapping("/orders")
    private ResponseEntity<?> createOrder(@RequestBody CustomerOrder customerOrder, UriComponentsBuilder uriComponentsBuilder) {
        if (customerOrderRepository.existsById(customerOrder.getId()))
            return new ResponseEntity<>(new ErrorInfo("An order with id %s already exists".formatted(customerOrder.getId())), HttpStatus.CONFLICT);

        customerOrderRepository.save(customerOrder);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(
                uriComponentsBuilder
                        .path("/api/orders/{id}")
                        .buildAndExpand(customerOrder.getId())
                        .toUri()
        );

        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @PutMapping("/orders/{id}")
    private ResponseEntity<?> updateOrder(@PathVariable long id, @RequestBody CustomerOrder customerOrder) {
        Optional<CustomerOrder> optOrder = customerOrderRepository.findById(id);

        if (optOrder.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Order with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        CustomerOrder currentCustomerOrder = optOrder.get();

        currentCustomerOrder.setCustomerName(customerOrder.getCustomerName());
        currentCustomerOrder.setDatetime(customerOrder.getDatetime());

        customerOrderRepository.save(currentCustomerOrder);

        return new ResponseEntity<>(currentCustomerOrder, HttpStatus.OK);
    }

    @DeleteMapping("/orders/{id}")
    private ResponseEntity<?> deleteOrder(@PathVariable long id) {
        if (!customerOrderRepository.existsById(id))
            return new ResponseEntity<>(new ErrorInfo("Order with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        customerOrderRepository.deleteById(id);

        return new ResponseEntity<>("Order with id %s deleted".formatted(id), HttpStatus.OK);
    }

    // TODO: List all books in an order, add book to an order, remove book from an order

    @GetMapping("/orders/{id}/books")
    private ResponseEntity<?> getOrderBooks(@PathVariable long id) {
        Optional<CustomerOrder> optOrder = customerOrderRepository.findById(id);

        if (optOrder.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Order with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        CustomerOrder customerOrder = optOrder.get();

        return new ResponseEntity<>(customerOrder.getBooks(), HttpStatus.OK);
    }

    @PostMapping("/orders/{id}/books")
    private ResponseEntity<?> createOrderBook(@PathVariable long id, @RequestBody Book book, UriComponentsBuilder uriComponentsBuilder) {
        Optional<CustomerOrder> optOrder = customerOrderRepository.findById(id);

        if (bookRepository.existsById(book.getISBN()))
            return new ResponseEntity<>(new ErrorInfo("A book with isbn %s already exists".formatted(book.getISBN())), HttpStatus.CONFLICT);

        if (optOrder.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Order with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        CustomerOrder customerOrder = optOrder.get();

        bookRepository.save(book);

        customerOrder.addBook(book);

        customerOrderRepository.save(customerOrder);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(
                uriComponentsBuilder
                        .path("/api/books/{isbn}")
                        .buildAndExpand(book.getISBN())
                        .toUri()
        );

        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @DeleteMapping("/orders/{id}/books/{isbn}")
    private ResponseEntity<?> deleteOrderBook(@PathVariable long id, @PathVariable String isbn) {
        Optional<CustomerOrder> optOrder = customerOrderRepository.findById(id);

        if (optOrder.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Order with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        if (!bookRepository.existsById(isbn))
            return new ResponseEntity<>(new ErrorInfo("Book with isbn %s does not exist".formatted(isbn)), HttpStatus.NOT_FOUND);

        CustomerOrder customerOrder = optOrder.get();

        customerOrder.removeBook(isbn);

        customerOrderRepository.save(customerOrder);

        return new ResponseEntity<>("Book %s removed from order %s".formatted(isbn, id), HttpStatus.OK);
    }
}

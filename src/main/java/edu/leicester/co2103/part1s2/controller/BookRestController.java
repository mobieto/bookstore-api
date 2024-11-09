package edu.leicester.co2103.part1s2.controller;

import edu.leicester.co2103.part1s2.domain.Author;
import edu.leicester.co2103.part1s2.domain.Book;
import edu.leicester.co2103.part1s2.domain.CustomerOrder;
import edu.leicester.co2103.part1s2.domain.ErrorInfo;
import edu.leicester.co2103.part1s2.repo.AuthorRepository;
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
public class BookRestController {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @GetMapping("/books")
    private ResponseEntity<?> getBooks() {
        List<Book> books = (List<Book>) bookRepository.findAll();

        return new ResponseEntity<>(
                books,
                books.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }

    @GetMapping("/books/{isbn}")
    private ResponseEntity<?> getBook(@PathVariable String isbn) {
        Optional<Book> book = bookRepository.findById(isbn);

        return book.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity(new ErrorInfo("Book with isbn %s not found".formatted(isbn)), HttpStatus.NOT_FOUND));
    }

    @PostMapping("/books")
    private ResponseEntity<?> createBook(@RequestBody Book book, UriComponentsBuilder uriComponentsBuilder) {
        if (bookRepository.existsById(book.getISBN())) {
            return new ResponseEntity<>(new ErrorInfo("A book with isbn %s already exists".formatted(book.getISBN())), HttpStatus.CONFLICT);
        }

        bookRepository.save(book);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(
                uriComponentsBuilder
                        .path("/api/books/{isbn}")
                        .buildAndExpand(book.getISBN())
                        .toUri()
        );

        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @PutMapping("/books/{isbn}")
    private ResponseEntity<?> updateBook(@PathVariable String isbn, @RequestBody Book book) {
        Optional<Book> targetBook = bookRepository.findById(isbn);

        if (targetBook.isEmpty()) {
            return new ResponseEntity<>(new ErrorInfo("Book with isbn %s does not exist".formatted(isbn)), HttpStatus.NOT_FOUND);
        }

        Book currentBook = targetBook.get();

        currentBook.setTitle(book.getTitle());
        currentBook.setPublicationYear(book.getPublicationYear());
        currentBook.setPrice(book.getPrice());

        bookRepository.save(currentBook);

        return new ResponseEntity<>(currentBook, HttpStatus.OK);
    }

    @DeleteMapping("/books/{isbn}")
    private ResponseEntity<?> deleteBook(@PathVariable String isbn) {
        Optional<Book> optBook = bookRepository.findById(isbn);

        if (optBook.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Book with id %s does not exist".formatted(isbn)), HttpStatus.NOT_FOUND);

        Book book = optBook.get();
        List<CustomerOrder> orders = (List<CustomerOrder>) customerOrderRepository.findAll();

        // Only get orders containing book to be deleted
        orders = orders.stream()
                .filter(order -> order.getBooks().stream()
                        .anyMatch(order_book -> order_book.getISBN()
                                .equals(isbn)))
                .toList();

        // Remove book reference from authors
        book.getAuthors()
                .forEach(author -> {
                    author.removeBook(isbn);
                    authorRepository.save(author);
                });

        // Remove book reference from orders
        orders.forEach(order -> {
            order.removeBook(isbn);
            customerOrderRepository.save(order);
        });

        bookRepository.deleteById(isbn);

        return new ResponseEntity<>("Book with id %s deleted".formatted(isbn), HttpStatus.OK);
    }

    @GetMapping("/books/{isbn}/authors")
    private ResponseEntity<?> getBookAuthors(@PathVariable String isbn) {
        Optional<Book> optBook = bookRepository.findById(isbn);

        if (optBook.isEmpty())
            return new ResponseEntity<>("Book with isbn %s does not exist".formatted(isbn), HttpStatus.NOT_FOUND);

        Book book = optBook.get();

        return new ResponseEntity<>(book.getAuthors(), HttpStatus.OK);
    }

    @GetMapping("/books/{isbn}/orders")
    private ResponseEntity<?> getOrdersContainingBook(@PathVariable String isbn) {
        if (!bookRepository.existsById(isbn))
            return new ResponseEntity<>("Book with isbn %s does not exist".formatted(isbn), HttpStatus.NOT_FOUND);

        List<CustomerOrder> orders = (List<CustomerOrder>) customerOrderRepository.findAll();

        // Get every order containing book
        orders = orders.stream()
                .filter(order -> order.getBooks().stream()
                        .anyMatch(book -> book.getISBN()
                                .equals(isbn)))
                .toList();

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthorRestController {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @GetMapping("/authors")
    private ResponseEntity<List<Author>> getAuthors() {
        List<Author> authors = (List<Author>) authorRepository.findAll();

        return new ResponseEntity<>(
                authors,
                authors.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK
        );
    }

    @GetMapping("/authors/{id}")
    private ResponseEntity<?> getAuthor(@PathVariable long id) {
        Optional<Author> author = authorRepository.findById(id);

        return author.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity(new ErrorInfo("Author with id %s not found".formatted(id)), HttpStatus.NOT_FOUND));
    }

    @PostMapping("/authors")
    private ResponseEntity<?> createAuthor(@RequestBody Author author, UriComponentsBuilder uriComponentsBuilder) {
        if (authorRepository.existsById(author.getId())) {
            return new ResponseEntity<>(new ErrorInfo("An author with id %s already exists".formatted(author.getId())), HttpStatus.CONFLICT);
        }

        authorRepository.save(author);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setLocation(
                uriComponentsBuilder
                        .path("/api/authors/{id}")
                        .buildAndExpand(author.getId())
                        .toUri()
        );

        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
    }

    @PutMapping("/authors/{id}")
    private ResponseEntity<?> updateAuthor(@PathVariable Long id, @RequestBody Author author) {
        Optional<Author> targetAuthor = authorRepository.findById(id);

        if (targetAuthor.isEmpty()) {
            return new ResponseEntity<>(new ErrorInfo("Author with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);
        }

        Author currentAuthor = targetAuthor.get();

        currentAuthor.setName(author.getName());
        currentAuthor.setBirthyear(author.getBirthyear());
        currentAuthor.setNationality(author.getNationality());

        authorRepository.save(currentAuthor);

        return new ResponseEntity<>(currentAuthor, HttpStatus.OK);
    }

    @DeleteMapping("/authors/{id}")
    private ResponseEntity<?> deleteAuthor(@PathVariable Long id) {
        Optional<Author> optAuthor = authorRepository.findById(id);

        if (optAuthor.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Author with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        Author author = optAuthor.get();
        List<Book> booksForRemoval = new ArrayList<>();

        // Remove author reference from every book and if book has no authors, mark for deletion
        author.getBooks()
                .forEach(book -> {
                    book.removeAuthor(author.getId());
                    bookRepository.save(book);

                    if (book.getAuthors().isEmpty()) {
                        booksForRemoval.add(book);
                    }
                });

        // Delete every marked book and remove references from orders
        booksForRemoval.forEach(book -> {
            List<CustomerOrder> orders = (List<CustomerOrder>) customerOrderRepository.findAll();

            author.removeBook(book.getISBN());
            authorRepository.save(author);

            // Get every order that contains book
            orders = orders.stream()
                    .filter(order -> order.getBooks().stream()
                            .anyMatch(order_book -> order_book.getISBN()
                                    .equals(book.getISBN())))
                    .toList();

            // Remove book reference from order
            orders.forEach(order -> {
                order.removeBook(book.getISBN());
                customerOrderRepository.save(order);
            });

            bookRepository.deleteById(book.getISBN());
        });

        authorRepository.deleteById(id);

        return new ResponseEntity<>("Author with id %s deleted".formatted(id), HttpStatus.OK);
    }

    @GetMapping("/authors/{id}/books")
    private ResponseEntity<?> getAuthorBooks(@PathVariable Long id) {
        Optional<Author> targetAuthor = authorRepository.findById(id);

        if (targetAuthor.isEmpty())
            return new ResponseEntity<>(new ErrorInfo("Author with id %s does not exist".formatted(id)), HttpStatus.NOT_FOUND);

        Author author = targetAuthor.get();

        return new ResponseEntity<>(author.getBooks(), HttpStatus.OK);
    }
}

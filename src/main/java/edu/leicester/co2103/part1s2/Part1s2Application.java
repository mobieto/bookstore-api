package edu.leicester.co2103.part1s2;

import edu.leicester.co2103.part1s2.domain.Author;
import edu.leicester.co2103.part1s2.domain.Book;
import edu.leicester.co2103.part1s2.domain.CustomerOrder;
import edu.leicester.co2103.part1s2.repo.AuthorRepository;
import edu.leicester.co2103.part1s2.repo.BookRepository;
import edu.leicester.co2103.part1s2.repo.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Part1s2Application implements CommandLineRunner {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    public static void main(String[] args) {
        SpringApplication.run(Part1s2Application.class, args);
    }


    @Override
    public void run(String... args) {
        Author author1 = new Author();
        Author author2 = new Author();

        Book book1 = new Book();
        Book book2 = new Book();

        CustomerOrder order1 = new CustomerOrder();
        CustomerOrder order2 = new CustomerOrder();

        book1.setISBN("HW1334");
        book1.setTitle("HELLO WORLD");

        book2.setISBN("TR1412");
        book2.setTitle("HELLO WORLD 2: UNLEASHED");

        order1.setCustomerName("John Cena");
        order2.setCustomerName("John Doe");

        bookRepository.save(book1);
        bookRepository.save(book2);

        author1.addBook(book1);
        author2.addBook(book1);
        author1.addBook(book2);

        book1.addAuthor(author1);
        book1.addAuthor(author2);
        book2.addAuthor(author1);

        order1.addBook(book1);
        order2.addBook(book1);
        order2.addBook(book2);

        authorRepository.save(author1);
        authorRepository.save(author2);
        bookRepository.save(book1);
        bookRepository.save(book2);
        customerOrderRepository.save(order1);
        customerOrderRepository.save(order2);
    }
}

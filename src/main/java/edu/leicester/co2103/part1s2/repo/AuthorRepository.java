package edu.leicester.co2103.part1s2.repo;

import edu.leicester.co2103.part1s2.domain.Author;
import org.springframework.data.repository.CrudRepository;

public interface AuthorRepository extends CrudRepository<Author,Long> { }

package edu.leicester.co2103.part1s2.repo;

import edu.leicester.co2103.part1s2.domain.CustomerOrder;
import org.springframework.data.repository.CrudRepository;

public interface CustomerOrderRepository extends CrudRepository<CustomerOrder, Long> { }
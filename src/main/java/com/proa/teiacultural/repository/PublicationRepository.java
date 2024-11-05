package com.proa.teiacultural.repository;

import com.proa.teiacultural.entities.Publication;
import com.proa.teiacultural.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    void deleteByUser(User user);
    List<Publication> findByUser(User user);


}

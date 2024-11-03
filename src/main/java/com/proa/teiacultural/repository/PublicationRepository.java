package com.proa.teiacultural.repository;

import com.proa.teiacultural.entities.Publication;
import com.proa.teiacultural.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    void deleteByUser(User user);

}

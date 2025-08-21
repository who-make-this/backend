package university.likelion.wmt.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.image.entity.Image;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByCfName(String cfName);
}

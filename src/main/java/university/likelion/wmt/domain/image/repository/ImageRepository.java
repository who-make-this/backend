package university.likelion.wmt.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}

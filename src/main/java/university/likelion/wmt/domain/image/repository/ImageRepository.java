package university.likelion.wmt.domain.image.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByCfName(String cfName);

    List<Image> findAllByRefTypeAndRefId(String refType, Long refId);
}

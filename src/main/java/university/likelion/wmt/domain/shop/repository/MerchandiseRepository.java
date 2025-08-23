package university.likelion.wmt.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.shop.entity.Merchandise;
import university.likelion.wmt.domain.shop.entity.MerchandiseType;

public interface MerchandiseRepository extends JpaRepository<Merchandise, Long> {
    Optional<Merchandise> findByIdAndIsVisibleTrue(Long id);

    List<Merchandise> findAllByIsVisibleTrueOrderByName();

    List<Merchandise> findAllByIsVisibleTrueAndTypeOrderByName(MerchandiseType type);
}

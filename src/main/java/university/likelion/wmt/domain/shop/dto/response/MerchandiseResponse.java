package university.likelion.wmt.domain.shop.dto.response;

import university.likelion.wmt.domain.shop.entity.Merchandise;
import university.likelion.wmt.domain.shop.entity.MerchandiseType;

public record MerchandiseResponse(
    Long id,
    String name,
    Long price,
    MerchandiseType type
) {
    public static MerchandiseResponse from(Merchandise merchandise) {
        return new MerchandiseResponse(merchandise.getId(), merchandise.getName(), merchandise.getPrice(),
            merchandise.getType());
    }
}

package university.likelion.wmt.domain.shop.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.entity.MileageLogReferenceType;
import university.likelion.wmt.domain.mileage.implement.MileageWriter;
import university.likelion.wmt.domain.order.entity.Order;
import university.likelion.wmt.domain.order.entity.OrderDetail;
import university.likelion.wmt.domain.order.implement.OrderWriter;
import university.likelion.wmt.domain.shop.dto.response.MerchandiseResponse;
import university.likelion.wmt.domain.shop.dto.response.VoucherCodeResponse;
import university.likelion.wmt.domain.shop.entity.Merchandise;
import university.likelion.wmt.domain.shop.entity.MerchandiseType;
import university.likelion.wmt.domain.shop.entity.VoucherCode;
import university.likelion.wmt.domain.shop.entity.VoucherStatus;
import university.likelion.wmt.domain.shop.exception.ShopErrorCode;
import university.likelion.wmt.domain.shop.exception.ShopException;
import university.likelion.wmt.domain.shop.repository.MerchandiseRepository;
import university.likelion.wmt.domain.shop.repository.VoucherCodeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {
    private final OrderWriter orderWriter;
    private final MileageWriter mileageWriter;

    private final MerchandiseRepository merchandiseRepository;
    private final VoucherCodeRepository voucherCodeRepository;

    public List<MerchandiseResponse> getMerchandiseList() {
        return merchandiseRepository.findAllByIsVisibleTrueOrderByName().stream()
            .map(MerchandiseResponse::from)
            .toList();
    }

    @Transactional
    public List<VoucherCodeResponse> purchase(Long userId, Long merchandiseId) {
        Order order = orderWriter.createOrder(userId, merchandiseId);

        mileageWriter.use(userId, order.getTotalPrice(), MileageLogReferenceType.ORDER, order.getId());

        List<VoucherCodeResponse> issued = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (OrderDetail orderDetail : order.getOrderDetails()) {
            Merchandise merchandise = orderDetail.getMerchandise();
            if (orderDetail.getMerchandise().getType() != MerchandiseType.GUMI_GIFT) {
                continue;
            }

            VoucherCode code = voucherCodeRepository.findFirstByMerchandiseAndStatusAndStartsAtLessThanEqualAndEndedAtGreaterThanEqualOrderByIdAsc(
                    merchandise, VoucherStatus.AVAILABLE, today, today)
                .orElseThrow(() -> new ShopException(ShopErrorCode.MERCHANDISE_NOT_PURCHASABLE));

            orderDetail.setVoucherCodeId(code.getId());
            code.used();

            issued.add(VoucherCodeResponse.from(code));
        }

        order.complete();

        return issued;
    }

    public List<VoucherCodeResponse> getCode(Long userId, Long orderId) {
        return voucherCodeRepository.findVoucherCodesByOrderAndUser(userId, orderId, MerchandiseType.GUMI_GIFT)
            .stream()
            .map(VoucherCodeResponse::from)
            .toList();
    }
}

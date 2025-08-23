package university.likelion.wmt.domain.shop.dto.response;

import university.likelion.wmt.domain.shop.entity.VoucherCode;

public record VoucherCodeResponse(
    String pinCode
) {
    public static VoucherCodeResponse from(VoucherCode voucherCode) {
        return new VoucherCodeResponse(voucherCode.getPinCode());
    }
}

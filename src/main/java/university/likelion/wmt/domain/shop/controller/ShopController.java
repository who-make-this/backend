package university.likelion.wmt.domain.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.shop.dto.response.MerchandiseResponse;
import university.likelion.wmt.domain.shop.dto.response.VoucherCodeResponse;
import university.likelion.wmt.domain.shop.service.ShopService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class ShopController {
    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<List<MerchandiseResponse>> listMerchandises() {
        return ResponseEntity.ok(shopService.getMerchandiseList());
    }

    @PostMapping("/purchase/{merchandiseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VoucherCodeResponse>> purchase(@AuthenticationPrincipal Long userId,
        @PathVariable Long merchandiseId) {
        return ResponseEntity.ok(shopService.purchase(userId, merchandiseId));
    }
}

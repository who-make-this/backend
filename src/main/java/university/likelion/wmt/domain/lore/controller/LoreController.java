package university.likelion.wmt.domain.lore.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.lore.dto.response.LoreResponse;
import university.likelion.wmt.domain.lore.service.LoreService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lore")
public class LoreController {
    private final LoreService loreService;

    @GetMapping("/{marketId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LoreResponse>> getUnlockedLore(@AuthenticationPrincipal Long userId,
        @PathVariable Long marketId) {
        return ResponseEntity.ok(loreService.getUnlocked(userId, marketId));
    }
}

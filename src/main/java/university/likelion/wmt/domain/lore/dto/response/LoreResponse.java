package university.likelion.wmt.domain.lore.dto.response;

import university.likelion.wmt.domain.lore.entity.Lore;

public record LoreResponse(
    String title,
    String content,
    Long requiredMissionCount
) {
    public static LoreResponse from(Lore lore) {
        return new LoreResponse(lore.getTitle(), lore.getContent(), lore.getRequiredMissionCount());
    }
}

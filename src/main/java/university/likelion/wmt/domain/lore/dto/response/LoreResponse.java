package university.likelion.wmt.domain.lore.dto.response;

public record LoreResponse(
    long requiredMissionCount,
    LoreData data
) {
    public record LoreData(
        String title,
        String content,
        String imageUrl
    ) {
    }
}

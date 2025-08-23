package university.likelion.wmt.domain.image.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.id.Tsid;

@Entity
@Table(
    name = "images",
    indexes = {
        @Index(name = "idx_images_ref_type", columnList = "ref_type")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @Tsid
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String cfName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 128)
    private String contentType;

    private String refType;

    private Long refId;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Image(String cfName, String imageUrl, Long fileSize, String contentType) {
        this.cfName = cfName;
        this.imageUrl = imageUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public void setReference(String refType, Long refId) {
        this.refType = refType;
        this.refId = refId;
    }
}

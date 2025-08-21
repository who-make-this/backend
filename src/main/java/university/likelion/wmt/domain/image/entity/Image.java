package university.likelion.wmt.domain.image.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.id.Tsid;

@Entity
@Table(name = "images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @Tsid
    private Long id;

    //cfName 길이 수정
    @Column(nullable = false, unique = true, length = 255)
    private String cfName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 128)
    private String contentType;

    private Long refId;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Image(String cfName, Long fileSize, String contentType) {
        this.cfName = cfName;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
}

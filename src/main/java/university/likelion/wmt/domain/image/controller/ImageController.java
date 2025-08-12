package university.likelion.wmt.domain.image.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.dto.response.ImageResponse;
import university.likelion.wmt.domain.image.service.ImageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(imageService.upload(file));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable("imageId") Long imageId) {
        imageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }
}

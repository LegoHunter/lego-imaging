package io.legohunter.imaging.service;

import io.legohunter.s3.minio.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoProcessingService {

    private final MinioService minioService;
    private final ImageScalingService imageScalingService;
    private final MetadataExtractorService metadataExtractorService;
    private final PhotoMetricsService photoMetricsService;

    private final ItemInventoryDao itemInventoryDao;
    private final ItemInventoryPhotoDao itemInventoryPhotoDao;

    private static final String FINAL_BUCKET = "lego-photos-sandbox";

    // =========================
    // Event-driven entry point
    // =========================
    public void process(PhotoUploadEvent event) {

        LoggingContext.init();
        long startTime = System.currentTimeMillis();
        String mode = "event";

        try {
            log.info("photo.process.start mode=event bucket={} key={}",
                    event.getBucket(), event.getObjectKey());

            try (InputStream is = minioService.getObject(
                    event.getBucket(),
                    event.getObjectKey())) {

                processInternal(is, event.getObjectKey(), true, event, mode);
            }

            photoMetricsService.incrementProcessed(mode);

            log.info("photo.process.success mode=event bucket={} key={}",
                    event.getBucket(), event.getObjectKey());

        } catch (Exception e) {
            photoMetricsService.incrementFailed(mode);

            log.error("photo.process.failed mode=event bucket={} key={}",
                    event.getBucket(), event.getObjectKey(), e);

            throw new RuntimeException(e);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            photoMetricsService.recordProcessingTime(duration, mode);

            log.info("photo.process.complete mode=event durationMs={}", duration);

            LoggingContext.clear();
        }
    }

    // =========================
    // Batch endpoint entry point
    // =========================
    public void process(MultipartFile file) {

        LoggingContext.init();
        long startTime = System.currentTimeMillis();
        String mode = "batch";

        try {
            log.info("photo.process.start mode=batch filename={}",
                    file.getOriginalFilename());

            try (InputStream is = file.getInputStream()) {

                processInternal(is, file.getOriginalFilename(), false, null, mode);
            }

            photoMetricsService.incrementProcessed(mode);

            log.info("photo.process.success mode=batch filename={}",
                    file.getOriginalFilename());

        } catch (Exception e) {
            photoMetricsService.incrementFailed(mode);

            log.error("photo.process.failed mode=batch filename={}",
                    file.getOriginalFilename(), e);

            throw new RuntimeException(e);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            photoMetricsService.recordProcessingTime(duration, mode);

            log.info("photo.process.complete mode=batch durationMs={}", duration);

            LoggingContext.clear();
        }
    }

    // =========================
    // Shared processing pipeline
    // =========================
    private void processInternal(
            InputStream originalStream,
            String originalFilename,
            boolean deleteSource,
            PhotoUploadEvent event,
            String mode
    ) throws Exception {

        // Add filename early to MDC
        if (originalFilename != null) {
            MDC.put("filename", originalFilename);
        }

        // =========================
        // Read original bytes
        // =========================
        byte[] originalBytes = originalStream.readAllBytes();

        log.info("photo.read.complete filename={} sizeBytes={}",
                originalFilename, originalBytes.length);

        // =========================
        // Extract metadata
        // =========================
        Map<String, String> keywords =
                metadataExtractorService.extractKeywords(originalBytes);

        String uuid = keywords.get("uuid");
        String bricklink = keywords.get("bl");

        if (uuid != null) MDC.put("uuid", uuid);
        if (bricklink != null) MDC.put("bricklink", bricklink);

        log.info("photo.metadata.extracted filename={} uuid={} bl={}",
                originalFilename, uuid, bricklink);

        if (uuid == null || bricklink == null) {
            log.error("photo.metadata.missing_required filename={} keywords={}",
                    originalFilename, keywords);
            throw new IllegalStateException("Missing required keywords (uuid, bl)");
        }

        // =========================
        // Scale image
        // =========================
        byte[] scaledBytes =
                imageScalingService.scale(originalBytes);

        log.info("photo.scaled filename={} originalSize={} scaledSize={}",
                originalFilename, originalBytes.length, scaledBytes.length);

        // =========================
        // Compute MD5
        // =========================
        String md5 =
                metadataExtractorService.calculateMd5(scaledBytes);

        MDC.put("md5", md5);

        log.info("photo.hash.computed md5={}", md5);

        // =========================
        // DB UPSERTS
        // =========================
        itemInventoryDao.upsertByUuid(uuid, bricklink);
        log.info("photo.db.inventory.upsert uuid={}", uuid);

        itemInventoryPhotoDao.upsertByMd5(md5, uuid);
        log.info("photo.db.photo.upsert md5={} uuid={}", md5, uuid);

        // =========================
        // Upload to MinIO
        // =========================
        String destinationKey = buildKey(bricklink, uuid, md5);

        minioService.putObject(
                FINAL_BUCKET,
                destinationKey,
                new ByteArrayInputStream(scaledBytes),
                scaledBytes.length,
                "image/jpeg"
        );

        log.info("photo.upload.success bucket={} key={}",
                FINAL_BUCKET, destinationKey);

        // =========================
        // Delete original (event mode only)
        // =========================
        if (deleteSource && event != null) {
            try {
                minioService.deleteObject(event.getBucket(), event.getObjectKey());

                log.info("photo.source.deleted bucket={} key={}",
                        event.getBucket(), event.getObjectKey());

            } catch (Exception e) {
                log.warn("photo.source.delete_failed bucket={} key={}",
                        event.getBucket(), event.getObjectKey(), e);
            }
        }
    }

    // =========================
    // Helper
    // =========================
    private String buildKey(String bl, String uuid, String md5) {
        return String.format("%s/%s/%s.jpg", bl, uuid, md5);
    }
}
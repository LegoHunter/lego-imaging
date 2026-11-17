package io.legohunter.imaging.flickr.config;

import io.legohunter.imaging.flickr.impl.FlickrPhotoServiceImpl;
import io.legohunter.imaging.service.hosting.api.ImageHostingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Services do not component-scan this library. They import the Flickr classes by name, exactly these three
 * (lego-data-ingress: {@code FlickrImageHostingConfiguration}). The Flickr service must start from them alone, so
 * every collaborator it needs has to be a bean of {@link FlickrConfiguration}, not a scanned component.
 */
class FlickrImportedByNameTest {

    @Test
    void theFlickrServiceStartsFromTheThreeClassesAServiceImports() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(FlickrProperties.class, FlickrConfiguration.class, FlickrPhotoServiceImpl.class)
                .withPropertyValues(
                        "flickr.userId=1@N04",
                        "flickr.secrets.key=test-key",
                        "flickr.secrets.secret=test-secret",
                        "flickr.secrets.token=test-token",
                        "flickr.secrets.tokenSecret=test-token-secret"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ImageHostingService.class);
                });
    }
}

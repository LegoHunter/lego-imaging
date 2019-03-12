package com.vattima.lego.imaging;

import com.vattima.lego.imaging.file.ImageCollector;
import com.vattima.lego.imaging.model.ImageFileHolder;
import com.vattima.lego.imaging.model.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SpringBootApplication
public class LegoImagingApplication {


    public static void main(String[] args) {
        SpringApplication.run(LegoImagingApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor
    private class ImageRunner implements ApplicationRunner {
        private final ImageCollector imageCollector;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            Map<String, InventoryItem> inventoryItemMap = new ConcurrentHashMap<>();
            imageCollector.getImagePaths()
                          .parallelStream()
                          .map(ImageFileHolder::new)
                          /* using path, extract all jpeg keywords out of jpg file */
                          /* move the image file from the root image folder into its own item subdirectory */
                          /* for all keywords, update the appropriate field in the bricklink_inventory item */
                          .forEach(ifh -> {
                              if (ifh.hasUuid()) {
                                  if (!inventoryItemMap.containsKey(ifh.getUuid())) {
                                      inventoryItemMap.put(ifh.getUuid(), new InventoryItem());
                                  }
                                  inventoryItemMap.get(ifh.getUuid()).getImageFiles().add(ifh);
                              }
                          });
            inventoryItemMap.keySet().forEach(k -> {
                System.out.println("["+k+"]");
            });
        }
    }

}

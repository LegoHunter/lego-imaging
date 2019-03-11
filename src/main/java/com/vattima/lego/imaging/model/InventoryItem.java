package com.vattima.lego.imaging.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@EqualsAndHashCode
public class InventoryItem {
    private String blItemNumber;
    private String uuid;
    private List<ImageFileHolder> imageFiles = new ArrayList<>();
}

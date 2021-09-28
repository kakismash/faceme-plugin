package com.dare.plugin;

public class FaceData {
    // Attributes from FaceMeDataManager.
    public long collectionId = -1;
    public long faceId       = -1;
    public String name;
    public float confidence  = 0;

    FaceData() {}

    public FaceData(long collectionId, long faceId, String name) {
        this.collectionId = collectionId;
        this.faceId       = faceId;
        this.name         = name;
    }
}

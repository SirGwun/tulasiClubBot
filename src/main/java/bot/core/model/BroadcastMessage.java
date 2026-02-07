package bot.core.model;

public class BroadcastMessage {
    private String text;
    private String photoFileId;

    public BroadcastMessage(String text, String photoFileId) {
        this.text = text;
        this.photoFileId = photoFileId;
    }

    public String getText() {
        return text;
    }

    public String getPhotoFileId() {
        return photoFileId;
    }

    public boolean hasPhoto() {
        return photoFileId != null && !photoFileId.isEmpty();
    }
}


package kibe.dev.genesis.Models;

public class Upload {
    private String Image, Title;

    public Upload() {

    }

    public Upload(String image, String title) {
        Image = image;
        Title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
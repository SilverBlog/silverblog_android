package org.SilverBlog.client;

public class Post_List_Serialzable {
    private String title;
    private String excerpt;

    public Post_List_Serialzable(String title, String excerpt) {
        this.title = title;
        this.excerpt = excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public String getTitle() {
        return title;
    }

}

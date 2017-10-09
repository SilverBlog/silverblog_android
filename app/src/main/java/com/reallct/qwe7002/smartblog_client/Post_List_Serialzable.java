package com.reallct.qwe7002.smartblog_client;

/**
 * Created by qwe7002 on 2017/10/8.
 */

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

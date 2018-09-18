package org.SilverBlog.client;

class content_json {
    private int post_id;
    private String content;
    private String sign;
    private String title;
    private String name;

    void setName(String name) {
        this.name = name;
    }

    void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setContent(String Content) {
        this.content = Content;
    }

    void setsign(String sign) {
        this.sign = sign;
    }
}

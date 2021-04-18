package br.projeto.worldnews.model;

public class Topic {


    private int id = -1;
    private String topic = "";
    private String topicTranslate = "";
    private String args = "";
    private boolean translated = false;

    public boolean isTranslated() {
        return translated;
    }

    public void setTranslated(boolean translated) {
        this.translated = translated;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopicTranslate() {
        return topicTranslate;
    }

    public void setTopicTranslate(String topicTranslated) {
        this.topicTranslate = topicTranslated;
    }


}

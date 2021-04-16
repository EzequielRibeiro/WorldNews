package br.projeto.worldnews.model;

public class Topic {


    private int id;
    private String topic;
    private String topicTranslated;

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

    public String getTopicTranslated() {
        return topicTranslated;
    }

    public void setTopicTranslated(String topicTranslated) {
        this.topicTranslated = topicTranslated;
    }




}

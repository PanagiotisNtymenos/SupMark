package com.supmark.model;

public class List {
    String listName;
    String listID;

    public List() {

    }

    public String getListName() {
        return listName;
    }

    public void setListName(String supermarket) {
        this.listName = listName;
    }

    public String getListID() {
        return listID;
    }

    public void setListID(String listID) {
        this.listID = listID;
    }

    public List(String listName, String listID) {
        this.listName = listName;
        this.listID = listID;
    }
}
